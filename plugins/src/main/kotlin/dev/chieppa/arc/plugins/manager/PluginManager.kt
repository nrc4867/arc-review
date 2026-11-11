package dev.chieppa.arc.plugins.manager

import dev.chieppa.arc.plugins.config.PluginSourceSpec
import dev.chieppa.arc.plugins.config.PluginsConfig
import dev.chieppa.arc.plugins.gitsource.GitSourceState
import dev.chieppa.arc.plugins.console.ConsoleProcesses
import dev.chieppa.arc.plugins.manifest.Capabilities
import dev.chieppa.arc.plugins.manifest.Capability
import dev.chieppa.arc.plugins.protocol.AppEventParams
import dev.chieppa.arc.plugins.protocol.SettingType
import dev.chieppa.arc.plugins.source.FoundPlugin
import dev.chieppa.arc.plugins.source.PluginDiscovery
import dev.chieppa.arc.plugins.typecheck.TypeErrors
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import java.nio.file.Path

/**
 * Every plugin Arc knows of, from the sources in its setup: finding them, starting the ones the person enabled,
 * reloading them when their folders change, and their settings. Only the person enables a plugin; after that, saves in
 * its folder reload it without asking. Changes to the setup are saved through [PluginServices.storage]; secret settings
 * go to [PluginServices.secrets], and removing a plugin's source forgets them.
 */
class PluginManager(private val services: PluginServices) {
    private val setup = MutableStateFlow(PluginsConfig())
    private val known = MutableStateFlow<List<PluginEntry>>(emptyList())
    private val watching = mutableMapOf<PluginSourceSpec, Job>()
    private val secrets = PluginSecrets(services.secrets)
    private val checks = TypeChecks(services.scope, services.typeChecker, services.typeCheckMillis)
    private val git = GitSources(services, services.gitSources, { setup.value }, ::save, { refresh(it) }) {
        known.value.mapNotNull { it.found.manifest }.associateBy { it.id }
    }

    val config: StateFlow<PluginsConfig> get() = setup

    /** The Node plugins run on, and whether it is on this machine already. */
    val nodeVersion: String? get() = services.nodeVersion

    fun nodeReady(): Boolean = services.nodeReady()

    /** The processes plugins run, which their Console components show. */
    val consoles: ConsoleProcesses get() = services.consoles

    /** Every plugin found, in source order. */
    val entries: StateFlow<List<PluginEntry>> get() = known

    /** Each running directory plugin's type errors, by id, found after it is enabled and after each save. */
    val typeErrors: StateFlow<Map<String, TypeErrors>> get() = checks.errors

    /** Each git source's state, by url: when it was checked, the update waiting, whether it failed. */
    val gitStates: StateFlow<Map<String, GitSourceState>> get() = git.states

    /**
     * Reads the setup, finds every source's plugins, starts those enabled, watches the folder sources and checks the git
     * sources for new commits.
     */
    fun start() {
        setup.value = services.storage.loadConfig()
        refresh()
        git.start()
        services.scope.launch {
            services.storage.configChanges().collectLatest {
                delay(services.reloadMillis)
                adoptSources()
            }
        }
    }

    /** Adds the git repo at [url] as a source at [ref] (a branch, tag or commit), pinned to the commit it names now. */
    fun addGit(url: String, ref: String) = git.add(url, ref)

    /** Moves git source [url]'s pin to the update waiting, reloading its plugins. */
    fun acceptUpdate(url: String) = git.accept(url)

    /** The folder git source [url] is cloned in. */
    fun gitClone(url: String): Path? = git.clone(url)

    fun addDirectory(path: Path) {
        val config = setup.value.withDirectory(path)
        if (config == setup.value) return
        save(config)
        refresh()
    }

    /**
     * Takes the sources added to the setup where it is kept since it was read, as `arc-review plugin new` adds them,
     * and nothing else: which plugins are enabled and what they may do stay the person's choice in this window.
     */
    @Synchronized
    private fun adoptSources() {
        val config = setup.value.withSourcesOf(services.storage.loadConfig())
        if (config == setup.value) return
        save(config)
        refresh()
    }

    fun removeSource(spec: PluginSourceSpec) {
        val sources = setup.value.sources - spec
        val kept = sources.flatMap(::find).map { it.id }.toSet()
        val gone = known.value.filter { it.found.source == spec && it.id !in kept }.map { it.id }
        save(gone.fold(setup.value.copy(sources = sources)) { config, id -> secrets.removeAll(config, id) })
        refresh()
        if (spec is PluginSourceSpec.Git) git.remove(spec)
    }

    /** Whether plugins' secret settings outlast this run of Arc, kept in the system's keychain. */
    val secretsLast: Boolean get() = secrets.lasting

    /**
     * Turns plugin [id] on, approving what its manifest asks for now, or off; as only the person does. A later manifest
     * that asks for more keeps it from running until it is turned on again.
     */
    fun setEnabled(id: String, on: Boolean) {
        val config = setup.value
        val capabilities = known.value.firstOrNull { it.id == id }?.found?.manifest?.capabilities
        save(
            if (on) {
                config.copy(enabled = config.enabled + id, approved = capabilities?.let { config.approved + (id to it) } ?: config.approved)
            } else {
                config.copy(enabled = config.enabled - id)
            },
        )
        refresh()
    }

    /** The running instance of plugin [id], if it is enabled. */
    fun instance(id: String): PluginInstance? = known.value.firstOrNull { it.id == id }?.instance

    /** Plugin [id]'s stored settings, its secret ones read from the keychain; only plugin [id] is given them. */
    fun settings(id: String): JsonObject {
        val config = setup.value
        return JsonObject(config.settings[id].orEmpty() + secrets.values(config, id))
    }

    /** Stores plugin [id]'s settings, its secret ones in the keychain, and tells it, so its open views redraw. */
    fun setSettings(id: String, values: JsonObject) {
        val declared = instance(id)?.contributions?.value?.settings.orEmpty().filterValues { it.type == SettingType.Secret }.keys
        save(secrets.keep(setup.value, id, values, declared + setup.value.secrets[id].orEmpty()))
        instance(id)?.settingsChanged()
    }

    /** Tells every running plugin [event] happened. */
    fun emit(event: AppEventParams) {
        known.value.forEach { it.instance?.emit(event) }
    }

    /** Stops every plugin, the watching and the Node process. */
    fun stop() {
        watching.values.forEach(Job::cancel)
        watching.clear()
        git.stop()
        known.value.forEach { it.instance?.stop() }
        services.shutdown()
    }

    @Synchronized
    private fun save(config: PluginsConfig) {
        setup.value = config
        services.storage.saveConfig(config)
    }

    /** Finds every source's plugins again and starts, stops or keeps each as the setup says. */
    @Synchronized
    private fun refresh(reload: PluginSourceSpec? = null) {
        val config = setup.value
        val found = config.sources.flatMap(::find).distinctBy { it.id }
        val before = known.value.associateBy { it.id }
        val entries = found.map { plugin -> entry(plugin, before[plugin.id], config, reload) }
        (before.keys - entries.map { it.id }.toSet()).forEach {
            before[it]?.instance?.stop()
            checks.clear(it)
        }
        known.value = entries
        watch(config.sources)
    }

    private fun entry(plugin: FoundPlugin, before: PluginEntry?, config: PluginsConfig, reload: PluginSourceSpec?): PluginEntry {
        val manifest = plugin.manifest
        val enabled = plugin.id in config.enabled
        val kept = before?.instance
        val asks = manifest?.let { Capability.added(it.capabilities, config.approved[plugin.id] ?: Capabilities()) }.orEmpty()
        if (manifest == null || !enabled || asks.isNotEmpty()) {
            kept?.stop()
            checks.clear(plugin.id)
            return PluginEntry(plugin, enabled, null, if (enabled) asks else emptyList())
        }
        val root = PluginDiscovery.root(plugin)
        if (kept != null) {
            kept.manifest = manifest
            kept.folder = plugin.folder
            kept.root = root
            if (reload == plugin.source) {
                kept.start()
                check(plugin)
            }
            return PluginEntry(plugin, true, kept)
        }
        val instance = PluginInstance(manifest, plugin.folder, root, services, { settings(plugin.id) }, { setSettings(plugin.id, it) })
        instance.start()
        check(plugin)
        return PluginEntry(plugin, true, instance)
    }

    private fun find(source: PluginSourceSpec): List<FoundPlugin> = PluginDiscovery.find(source, git::folder)

    /** Type checks [plugin] when it comes from a folder the person edits. */
    private fun check(plugin: FoundPlugin) {
        val manifest = plugin.manifest ?: return
        if (plugin.source is PluginSourceSpec.Directory) checks.request(plugin.id, plugin.folder, manifest.entry)
    }

    private fun watch(sources: List<PluginSourceSpec>) {
        (watching.keys - sources.toSet()).forEach { watching.remove(it)?.cancel() }
        sources.filterIsInstance<PluginSourceSpec.Directory>().filter { it !in watching }.forEach { spec ->
            watching[spec] = services.scope.launch {
                services.watcher.changes(Path.of(spec.path)).collectLatest {
                    delay(services.reloadMillis)
                    refresh(reload = spec)
                }
            }
        }
    }
}

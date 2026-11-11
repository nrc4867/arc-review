package dev.chieppa.arc.plugins.manager

import dev.chieppa.arc.plugins.config.PluginSourceSpec
import dev.chieppa.arc.plugins.config.PluginsConfig
import dev.chieppa.arc.plugins.console.ConsoleProcesses
import dev.chieppa.arc.plugins.manifest.Capabilities
import dev.chieppa.arc.plugins.manifest.Capability
import dev.chieppa.arc.plugins.protocol.AppEventParams
import dev.chieppa.arc.plugins.source.FoundPlugin
import dev.chieppa.arc.plugins.source.PluginDiscovery
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
 * its folder reload it without asking. Changes to the setup are saved through [PluginServices.storage].
 */
class PluginManager(private val services: PluginServices) {
    private val setup = MutableStateFlow(PluginsConfig())
    private val known = MutableStateFlow<List<PluginEntry>>(emptyList())
    private val watching = mutableMapOf<PluginSourceSpec, Job>()

    val config: StateFlow<PluginsConfig> get() = setup

    /** The Node plugins run on, and whether it is on this machine already. */
    val nodeVersion: String? get() = services.nodeVersion

    fun nodeReady(): Boolean = services.nodeReady()

    /** The processes plugins run, which their Console components show. */
    val consoles: ConsoleProcesses get() = services.consoles

    /** Every plugin found, in source order. */
    val entries: StateFlow<List<PluginEntry>> get() = known

    /** Reads the setup, finds every source's plugins, starts those enabled and watches the sources. */
    fun start() {
        setup.value = services.storage.loadConfig()
        refresh()
    }

    fun addDirectory(path: Path) {
        val spec = PluginSourceSpec.Directory(path.toAbsolutePath().normalize().toString())
        if (spec in setup.value.sources) return
        save(setup.value.copy(sources = setup.value.sources + spec))
        refresh()
    }

    fun removeSource(spec: PluginSourceSpec) {
        save(setup.value.copy(sources = setup.value.sources - spec))
        refresh()
    }

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

    /** Plugin [id]'s stored settings. */
    fun settings(id: String): JsonObject = setup.value.settings[id] ?: JsonObject(emptyMap())

    /** Stores plugin [id]'s settings and tells it, so its open views redraw. */
    fun setSettings(id: String, values: JsonObject) {
        save(setup.value.copy(settings = setup.value.settings + (id to values)))
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
        val found = config.sources.flatMap(PluginDiscovery::find).distinctBy { it.id }
        val before = known.value.associateBy { it.id }
        val entries = found.map { plugin -> entry(plugin, before[plugin.id], config, reload) }
        (before.keys - entries.map { it.id }.toSet()).forEach { before[it]?.instance?.stop() }
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
            return PluginEntry(plugin, enabled, null, if (enabled) asks else emptyList())
        }
        val root = PluginDiscovery.root(plugin)
        if (kept != null) {
            kept.manifest = manifest
            kept.folder = plugin.folder
            kept.root = root
            if (reload == plugin.source) kept.start()
            return PluginEntry(plugin, true, kept)
        }
        val instance = PluginInstance(manifest, plugin.folder, root, services, { settings(plugin.id) }, { setSettings(plugin.id, it) })
        instance.start()
        return PluginEntry(plugin, true, instance)
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

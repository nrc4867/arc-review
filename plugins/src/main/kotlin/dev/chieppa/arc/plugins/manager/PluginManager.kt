package dev.chieppa.arc.plugins.manager

import dev.chieppa.arc.plugins.config.PluginSourceSpec
import dev.chieppa.arc.plugins.config.PluginsConfig
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

    /** Turns plugin [id] on or off, as only the person does. */
    fun setEnabled(id: String, on: Boolean) {
        val enabled = if (on) setup.value.enabled + id else setup.value.enabled - id
        save(setup.value.copy(enabled = enabled))
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

    /** Stops every plugin and the watching. */
    fun stop() {
        watching.values.forEach(Job::cancel)
        watching.clear()
        known.value.forEach { it.instance?.stop() }
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
        if (manifest == null || !enabled) {
            kept?.stop()
            return PluginEntry(plugin, enabled, null)
        }
        if (kept != null) {
            kept.manifest = manifest
            kept.folder = plugin.folder
            if (reload == plugin.source) kept.start()
            return PluginEntry(plugin, true, kept)
        }
        val instance = PluginInstance(manifest, plugin.folder, services, { settings(plugin.id) }, { setSettings(plugin.id, it) })
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

package dev.chieppa.arc.store.plugins

import dev.chieppa.arc.plugins.config.PluginStorage
import dev.chieppa.arc.plugins.config.PluginsConfig
import dev.chieppa.arc.plugins.source.FileWatcher
import dev.chieppa.arc.plugins.source.NioFileWatcher
import dev.chieppa.arc.store.files.StoreJson
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.JsonObject
import java.nio.file.Path

/**
 * The plugins' setup in the JSON file [config] (their sources, which are enabled, each one's settings) and each
 * plugin's private store as `<id>.json` in [stores]. A missing or unreadable file reads as empty. [watcher] says when
 * the setup's file changes, as when `arc-review plugin new` adds a source to it.
 */
class PluginFiles(private val config: Path, private val stores: Path, private val watcher: FileWatcher = NioFileWatcher) : PluginStorage {
    override fun loadConfig(): PluginsConfig = StoreJson.load(config, PluginsConfig.serializer()) ?: PluginsConfig()

    override fun saveConfig(config: PluginsConfig) {
        StoreJson.save(this.config, PluginsConfig.serializer(), config)
    }

    override fun configChanges(): Flow<Unit> = watcher.changes(config)

    override fun loadStore(plugin: String): JsonObject = StoreJson.load(storeOf(plugin), JsonObject.serializer()) ?: JsonObject(emptyMap())

    override fun saveStore(plugin: String, store: JsonObject) {
        StoreJson.save(storeOf(plugin), JsonObject.serializer(), store)
    }

    private fun storeOf(plugin: String): Path = stores.resolve("$plugin$JSON")

    private companion object {
        const val JSON = ".json"
    }
}

package dev.chieppa.arc.plugins.config

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.serialization.json.JsonObject

/** Where the plugins' setup and each plugin's private store are kept between runs. */
interface PluginStorage {
    fun loadConfig(): PluginsConfig

    fun saveConfig(config: PluginsConfig)

    /** Says when the setup changed where it is kept, as when `arc-review plugin new` adds a source; never by default. */
    fun configChanges(): Flow<Unit> = emptyFlow()

    /** Plugin [plugin]'s private store, empty when it has none. */
    fun loadStore(plugin: String): JsonObject

    fun saveStore(plugin: String, store: JsonObject)

    /** Keeps nothing, for tests and previews; [changes] says when the setup changed elsewhere. */
    class InMemory(private var config: PluginsConfig = PluginsConfig(), private val changes: Flow<Unit> = emptyFlow()) : PluginStorage {
        private val stores = mutableMapOf<String, JsonObject>()

        override fun loadConfig(): PluginsConfig = config

        override fun saveConfig(config: PluginsConfig) {
            this.config = config
        }

        override fun configChanges(): Flow<Unit> = changes

        override fun loadStore(plugin: String): JsonObject = stores[plugin] ?: JsonObject(emptyMap())

        override fun saveStore(plugin: String, store: JsonObject) {
            stores[plugin] = store
        }
    }
}

package dev.chieppa.arc.plugins.config

import kotlinx.serialization.json.JsonObject

/** Where the plugins' setup and each plugin's private store are kept between runs. */
interface PluginStorage {
    fun loadConfig(): PluginsConfig

    fun saveConfig(config: PluginsConfig)

    /** Plugin [plugin]'s private store, empty when it has none. */
    fun loadStore(plugin: String): JsonObject

    fun saveStore(plugin: String, store: JsonObject)

    /** Keeps nothing, for tests and previews. */
    class InMemory(private var config: PluginsConfig = PluginsConfig()) : PluginStorage {
        private val stores = mutableMapOf<String, JsonObject>()

        override fun loadConfig(): PluginsConfig = config

        override fun saveConfig(config: PluginsConfig) {
            this.config = config
        }

        override fun loadStore(plugin: String): JsonObject = stores[plugin] ?: JsonObject(emptyMap())

        override fun saveStore(plugin: String, store: JsonObject) {
            stores[plugin] = store
        }
    }
}

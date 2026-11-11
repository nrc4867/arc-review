package dev.chieppa.arc.plugins.manager

import dev.chieppa.arc.plugins.config.PluginsConfig
import dev.chieppa.arc.plugins.secrets.SecretStore
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap

/**
 * Plugins' secret settings: the values in [store], remembered once read, and in the setup only which keys hold one. A
 * value the store refuses is kept for this run only, and from then on secrets count as not lasting.
 */
internal class PluginSecrets(private val store: SecretStore) {
    private val known = ConcurrentHashMap<Pair<String, String>, String>()

    @Volatile
    private var refused = false

    /** Whether secrets outlast this run of Arc here. */
    val lasting: Boolean get() = store.lasting && !refused

    /** Plugin [plugin]'s secret values for the keys [config] says hold one. */
    fun values(config: PluginsConfig, plugin: String): Map<String, JsonElement> =
        config.secrets[plugin].orEmpty().mapNotNull { key -> read(plugin, key)?.let { key to JsonPrimitive(it) } }.toMap()

    /**
     * [config] with plugin [plugin]'s [values] kept: those under [secretKeys] go to the store, an empty one forgetting
     * its key, and the rest to the setup's settings.
     */
    fun keep(config: PluginsConfig, plugin: String, values: JsonObject, secretKeys: Set<String>): PluginsConfig {
        val (secret, plain) = values.entries.partition { it.key in secretKeys }
        var kept = config.secrets[plugin].orEmpty()
        for ((key, value) in secret) {
            val text = runCatching { value.jsonPrimitive.contentOrNull }.getOrNull().orEmpty()
            kept = if (text.isEmpty()) {
                forget(plugin, key)
                kept - key
            } else {
                write(plugin, key, text)
                kept + key
            }
        }
        val secrets = if (kept.isEmpty()) config.secrets - plugin else config.secrets + (plugin to kept)
        return config.copy(settings = config.settings + (plugin to JsonObject(plain.associate { it.key to it.value })), secrets = secrets)
    }

    /** [config] with every secret of plugin [plugin] gone from the store and the setup. */
    fun removeAll(config: PluginsConfig, plugin: String): PluginsConfig {
        config.secrets[plugin].orEmpty().forEach { forget(plugin, it) }
        return config.copy(secrets = config.secrets - plugin)
    }

    private fun read(plugin: String, key: String): String? = known[plugin to key] ?: try {
        store.read(plugin, key)?.also { known[plugin to key] = it }
    } catch (_: IOException) {
        null
    }

    private fun write(plugin: String, key: String, value: String) {
        if (known[plugin to key] == value) return
        known[plugin to key] = value
        try {
            store.write(plugin, key, value)
        } catch (_: IOException) {
            refused = true
        }
    }

    private fun forget(plugin: String, key: String) {
        known.remove(plugin to key)
        try {
            store.delete(plugin, key)
        } catch (_: IOException) {
            // Nothing more to forget here.
        }
    }
}

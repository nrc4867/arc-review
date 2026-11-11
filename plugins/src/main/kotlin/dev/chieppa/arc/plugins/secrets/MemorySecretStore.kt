package dev.chieppa.arc.plugins.secrets

import java.util.concurrent.ConcurrentHashMap

/** Secrets kept in memory only, gone when Arc closes: for systems with no keychain Arc uses, and for tests. */
class MemorySecretStore(override val lasting: Boolean = false) : SecretStore {
    private val kept = ConcurrentHashMap<Pair<String, String>, String>()

    override fun read(plugin: String, key: String): String? = kept[plugin to key]

    override fun write(plugin: String, key: String, value: String) {
        kept[plugin to key] = value
    }

    override fun delete(plugin: String, key: String) {
        kept.remove(plugin to key)
    }
}

package dev.chieppa.arc.plugins.secrets

/**
 * Where plugins' secret settings are kept, by plugin id and setting key: the system's keychain, or memory where Arc
 * has none to use. Each call throws [java.io.IOException] when the keychain refuses it.
 */
interface SecretStore {
    /** Whether what it keeps outlasts this run of Arc. */
    val lasting: Boolean

    /** The secret plugin [plugin] keeps under [key], or null when there is none. */
    fun read(plugin: String, key: String): String?

    fun write(plugin: String, key: String, value: String)

    /** Forgets the secret under [key]; nothing happens when there is none. */
    fun delete(plugin: String, key: String)
}

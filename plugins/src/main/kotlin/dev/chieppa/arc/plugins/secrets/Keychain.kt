package dev.chieppa.arc.plugins.secrets

/**
 * The system's keychain, holding secrets by [service] and [account] for this person on this machine. Each call throws
 * [java.io.IOException] when the keychain refuses it.
 */
interface Keychain {
    /** The secret kept as [account] of [service], or null when there is none. */
    fun read(service: String, account: String): String?

    fun write(service: String, account: String, value: String)

    /** Forgets the secret; nothing happens when there is none. */
    fun delete(service: String, account: String)
}

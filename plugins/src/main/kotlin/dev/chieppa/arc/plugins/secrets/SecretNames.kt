package dev.chieppa.arc.plugins.secrets

/** What Arc names a secret in the system's keychain. */
object SecretNames {
    /** The service every plugin secret is kept under. */
    const val SERVICE = "dev.chieppa.arc.plugins"

    /** The account a secret is kept as: its owner, such as a plugin, and its key, such as a setting. */
    fun account(owner: String, key: String): String = "$owner/$key"

    /** The one name Windows Credential Manager keeps a secret by. */
    fun target(service: String, account: String): String = "$service/$account"
}

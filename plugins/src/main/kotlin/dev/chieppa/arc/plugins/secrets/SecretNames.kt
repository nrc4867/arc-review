package dev.chieppa.arc.plugins.secrets

/** What Arc names a plugin's secret in the system's keychain. */
object SecretNames {
    /** The service every plugin secret is kept under. */
    const val SERVICE = "dev.chieppa.arc.plugins"

    /** The account a secret is kept as: the plugin and its setting. */
    fun account(plugin: String, key: String): String = "$plugin/$key"

    /** The one name Windows Credential Manager keeps a secret by. */
    fun target(plugin: String, key: String): String = "$SERVICE/${account(plugin, key)}"
}

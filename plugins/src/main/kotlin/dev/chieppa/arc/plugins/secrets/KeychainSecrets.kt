package dev.chieppa.arc.plugins.secrets

/**
 * Secrets kept in [keychain] under [service], each as the account [SecretNames.account] names. Every failure of the
 * keychain, however it fails, is thrown as a [java.io.IOException].
 */
class KeychainSecrets(private val keychain: Keychain, private val service: String) : SecretStore {
    override val lasting: Boolean = true

    override fun read(plugin: String, key: String): String? = keychainCall { keychain.read(service, SecretNames.account(plugin, key)) }

    override fun write(plugin: String, key: String, value: String) = keychainCall { keychain.write(service, SecretNames.account(plugin, key), value) }

    override fun delete(plugin: String, key: String) = keychainCall { keychain.delete(service, SecretNames.account(plugin, key)) }
}

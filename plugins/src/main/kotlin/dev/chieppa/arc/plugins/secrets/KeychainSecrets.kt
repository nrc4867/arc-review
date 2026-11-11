package dev.chieppa.arc.plugins.secrets

/** Secrets kept in [keychain] under [service], each as the account [SecretNames.account] names. */
class KeychainSecrets(private val keychain: Keychain, private val service: String) : SecretStore {
    override val lasting: Boolean = true

    override fun read(plugin: String, key: String): String? = keychain.read(service, SecretNames.account(plugin, key))

    override fun write(plugin: String, key: String, value: String) = keychain.write(service, SecretNames.account(plugin, key), value)

    override fun delete(plugin: String, key: String) = keychain.delete(service, SecretNames.account(plugin, key))
}

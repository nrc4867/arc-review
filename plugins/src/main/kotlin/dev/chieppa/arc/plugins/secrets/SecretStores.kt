package dev.chieppa.arc.plugins.secrets

/**
 * The secret store for this system under [service]: Credential Manager on Windows, the Keychain on macOS, memory
 * elsewhere.
 */
object SecretStores {
    private const val WINDOWS = "windows"
    private const val MAC = "mac"

    fun system(service: String = SecretNames.SERVICE, osName: String = System.getProperty("os.name").orEmpty()): SecretStore = when {
        osName.lowercase().startsWith(WINDOWS) -> KeychainSecrets(WindowsCredentials, service)
        osName.lowercase().startsWith(MAC) -> KeychainSecrets(MacKeychain, service)
        else -> MemorySecretStore()
    }
}

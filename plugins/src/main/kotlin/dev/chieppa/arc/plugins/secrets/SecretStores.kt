package dev.chieppa.arc.plugins.secrets

/** The secret store for this system: Credential Manager on Windows, the Keychain on macOS, memory elsewhere. */
object SecretStores {
    private const val WINDOWS = "windows"
    private const val MAC = "mac"

    fun system(osName: String = System.getProperty("os.name").orEmpty()): SecretStore = when {
        osName.lowercase().startsWith(WINDOWS) -> WindowsCredentials
        osName.lowercase().startsWith(MAC) -> MacKeychain
        else -> MemorySecretStore()
    }
}

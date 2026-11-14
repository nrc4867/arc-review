package dev.chieppa.arc.tools

import java.security.GeneralSecurityException
import java.security.KeyStore
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509ExtendedTrustManager

/**
 * HTTPS that trusts what the operating system trusts as well as Java's own certificates: the macOS keychains or the
 * Windows root store. A network that inspects TLS signs with a root its owner adds to the system, which Java's
 * certificates alone do not hold.
 */
object SystemTrust {
    private const val PROTOCOL = "TLS"

    /** Makes the trust of [context] the default every HTTPS connection without its own uses. */
    fun install() {
        runCatching { SSLContext.setDefault(context()) }
    }

    /** A context trusting Java's certificates and those of the system's stores that load here. */
    fun context(): SSLContext {
        val managers = listOfNotNull(manager(null)) + systemStores().mapNotNull { load(it) }.mapNotNull { manager(it) }
        return SSLContext.getInstance(PROTOCOL).apply { init(null, arrayOf(AnyOfTrustManager(managers)), null) }
    }

    /** The key store types holding the system's trusted certificates on this host. */
    private fun systemStores(): List<String> = when {
        HostPlatform.isWindows -> listOf("Windows-ROOT")
        HostPlatform.isMac -> listOf("KeychainStore", "KeychainStore-ROOT")
        else -> emptyList()
    }

    private fun load(type: String): KeyStore? = try {
        KeyStore.getInstance(type).apply { load(null, null) }
    } catch (_: Exception) {
        null
    }

    /** The trust manager of [store], or of Java's own certificates when it is null. */
    private fun manager(store: KeyStore?): X509ExtendedTrustManager? = try {
        TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
            .apply { init(store) }
            .trustManagers.filterIsInstance<X509ExtendedTrustManager>().firstOrNull()
    } catch (_: GeneralSecurityException) {
        null
    }
}

package dev.chieppa.arc.tools

import java.net.Socket
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.SSLEngine
import javax.net.ssl.X509ExtendedTrustManager

/**
 * Trusts a certificate chain when any of [managers] does, trying them in order; when none does, the first one's
 * refusal is thrown. Issuers accepted are all of theirs.
 */
class AnyOfTrustManager(private val managers: List<X509ExtendedTrustManager>) : X509ExtendedTrustManager() {
    init {
        require(managers.isNotEmpty()) { "No trust managers" }
    }

    override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) = any { it.checkClientTrusted(chain, authType) }

    override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String, socket: Socket?) =
        any { it.checkClientTrusted(chain, authType, socket) }

    override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String, engine: SSLEngine?) =
        any { it.checkClientTrusted(chain, authType, engine) }

    override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) = any { it.checkServerTrusted(chain, authType) }

    override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String, socket: Socket?) =
        any { it.checkServerTrusted(chain, authType, socket) }

    override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String, engine: SSLEngine?) =
        any { it.checkServerTrusted(chain, authType, engine) }

    override fun getAcceptedIssuers(): Array<X509Certificate> = managers.flatMap { it.acceptedIssuers.asList() }.distinct().toTypedArray()

    private fun any(check: (X509ExtendedTrustManager) -> Unit) {
        var first: CertificateException? = null
        for (manager in managers) {
            try {
                check(manager)
                return
            } catch (e: CertificateException) {
                if (first == null) first = e
            }
        }
        throw checkNotNull(first)
    }
}

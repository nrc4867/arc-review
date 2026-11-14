package dev.chieppa.arc.tools

import java.net.Socket
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.SSLEngine
import javax.net.ssl.X509ExtendedTrustManager
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

class AnyOfTrustManagerTest {
    @Test
    fun aChainOneManagerTrustsIsTrusted() {
        val refusing = Fixed(CertificateException("unknown root"))
        val trusting = Fixed(null)
        AnyOfTrustManager(listOf(refusing, trusting)).checkServerTrusted(emptyArray(), AUTH, null as SSLEngine?)
        assertEquals(1, refusing.checks)
        assertEquals(1, trusting.checks)
    }

    @Test
    fun aChainNoManagerTrustsFailsWithTheFirstRefusal() {
        val first = CertificateException("PKIX path building failed")
        val manager = AnyOfTrustManager(listOf(Fixed(first), Fixed(CertificateException("second"))))
        assertSame(first, assertFailsWith<CertificateException> { manager.checkServerTrusted(emptyArray(), AUTH, null as Socket?) })
    }

    @Test
    fun theSystemsContextBuilds() {
        assertEquals("TLS", SystemTrust.context().protocol)
    }

    /** A trust manager that refuses with [refusal], or trusts every chain when it is null, counting its [checks]. */
    private class Fixed(private val refusal: CertificateException?) : X509ExtendedTrustManager() {
        var checks = 0

        private fun check() {
            checks++
            refusal?.let { throw it }
        }

        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) = check()
        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String, socket: Socket?) = check()
        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String, engine: SSLEngine?) = check()
        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) = check()
        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String, socket: Socket?) = check()
        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String, engine: SSLEngine?) = check()
        override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
    }

    private companion object {
        const val AUTH = "RSA"
    }
}

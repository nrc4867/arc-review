package dev.chieppa.arc.plugins.secrets

import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

class KeychainSecretsTest {
    private class FailingKeychain(private val failure: Throwable) : Keychain {
        override fun read(service: String, account: String): String? = throw failure

        override fun write(service: String, account: String, value: String) = throw failure

        override fun delete(service: String, account: String) = throw failure
    }

    @Test
    fun aLibraryThatCannotBeOpenedIsAnIOExceptionWithItsReason() {
        val store = KeychainSecrets(FailingKeychain(IllegalArgumentException("Cannot open library: Security")), SERVICE)
        assertEquals("Cannot open library: Security", assertFailsWith<IOException> { store.read("p", "k") }.message)
        assertFailsWith<IOException> { store.write("p", "k", "v") }
        assertFailsWith<IOException> { store.delete("p", "k") }
    }

    @Test
    fun aMissingSymbolIsAnIOException() {
        val store = KeychainSecrets(FailingKeychain(UnsatisfiedLinkError("No SecKeychainFindGenericPassword")), SERVICE)
        assertEquals("No SecKeychainFindGenericPassword", assertFailsWith<IOException> { store.read("p", "k") }.message)
    }

    @Test
    fun aRefusalStaysAsItIs() {
        val refusal = IOException("SecKeychainFindGenericPassword failed with -25293")
        assertSame(refusal, assertFailsWith<IOException> { KeychainSecrets(FailingKeychain(refusal), SERVICE).read("p", "k") })
    }

    @Test
    fun theKeychainIsAskedUnderTheServiceAndTheAccountItsOwnerAndKeyName() {
        val asked = mutableListOf<Pair<String, String>>()
        val keychain = object : Keychain {
            override fun read(service: String, account: String): String? = null.also { asked += service to account }

            override fun write(service: String, account: String, value: String) = Unit

            override fun delete(service: String, account: String) = Unit
        }
        KeychainSecrets(keychain, SERVICE).read("plugin", "token")
        assertEquals(listOf(SERVICE to "plugin/token"), asked)
    }

    private companion object {
        const val SERVICE = "dev.chieppa.arc.test"
    }
}

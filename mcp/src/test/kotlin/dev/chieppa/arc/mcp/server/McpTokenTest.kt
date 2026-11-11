package dev.chieppa.arc.mcp.server

import dev.chieppa.arc.plugins.secrets.MemorySecretStore
import dev.chieppa.arc.plugins.secrets.SecretStore
import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class McpTokenTest {
    @Test
    fun theTokenIsMadeOnceAndKeptInTheKeychain() {
        val keychain = MemorySecretStore(lasting = true)
        val token = McpToken(keychain).current()
        assertTrue(token.length >= 43)
        assertEquals(token, keychain.read(McpConstants.TOKEN_OWNER, McpConstants.TOKEN_KEY))
        assertEquals(token, McpToken(keychain).current())
        assertTrue(McpToken(keychain).lasting)
        assertNotEquals(token, McpToken(MemorySecretStore(lasting = true)).current())
    }

    @Test
    fun aKeychainThatRefusesKeepsItForThisRunOnly() {
        val refusing = object : SecretStore {
            override val lasting = true

            override fun read(plugin: String, key: String): String? = null

            override fun write(plugin: String, key: String, value: String) = throw IOException("refused")

            override fun delete(plugin: String, key: String) = Unit
        }
        val token = McpToken(refusing)
        assertEquals(token.current(), token.current())
        assertFalse(token.lasting)
    }
}

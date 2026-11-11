package dev.chieppa.arc.mcp.server

import dev.chieppa.arc.plugins.secrets.SecretStore
import java.io.IOException
import java.security.SecureRandom
import java.util.Base64

/**
 * The bearer token a client needs for Arc's MCP server: made at random the first time it is asked for and kept in
 * [store], the system's keychain, so it lasts for this install. Where the keychain refuses it, it lasts until Arc
 * closes, and [lasting] says so. It is never written to a file.
 */
class McpToken(private val store: SecretStore, private val random: SecureRandom = SecureRandom()) {
    @Volatile
    private var kept: String? = null

    @Volatile
    private var refused = false

    /** Whether the token outlasts this run of Arc. */
    val lasting: Boolean get() = store.lasting && !refused

    /** The token, made and kept the first time. */
    @Synchronized
    fun current(): String {
        kept?.let { return it }
        val stored = try {
            store.read(McpConstants.TOKEN_OWNER, McpConstants.TOKEN_KEY)
        } catch (_: IOException) {
            refused = true
            null
        }
        val token = stored ?: make().also(::keep)
        kept = token
        return token
    }

    private fun keep(token: String) {
        try {
            store.write(McpConstants.TOKEN_OWNER, McpConstants.TOKEN_KEY, token)
        } catch (_: IOException) {
            refused = true
        }
    }

    private fun make(): String {
        val bytes = ByteArray(BYTES).also(random::nextBytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    private companion object {
        const val BYTES = 32
    }
}

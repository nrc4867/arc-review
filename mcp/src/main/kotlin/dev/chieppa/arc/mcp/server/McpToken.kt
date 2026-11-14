package dev.chieppa.arc.mcp.server

import dev.chieppa.arc.plugins.secrets.SecretStore
import java.security.SecureRandom
import java.util.Base64

/**
 * The bearer token a client needs for Arc's MCP server: made at random the first time it is asked for and kept in
 * [store], the system's keychain, so it lasts for this install. Where the keychain fails, however it fails, the token
 * lasts until Arc closes, [lasting] says so and [problem] says why. It is never written to a file. Reading or keeping
 * it may wait on the keychain, so it is asked for away from the window's thread.
 */
class McpToken(private val store: SecretStore, private val random: SecureRandom = SecureRandom()) {
    @Volatile
    private var kept: String? = null

    /** Why the keychain did not keep the token, or null while it does. */
    @Volatile
    var problem: String? = null
        private set

    /** Whether the token outlasts this run of Arc. */
    val lasting: Boolean get() = store.lasting && problem == null

    /** The token, made and kept the first time. */
    @Synchronized
    fun current(): String {
        kept?.let { return it }
        val stored = keychain { store.read(McpConstants.TOKEN_OWNER, McpConstants.TOKEN_KEY) }
        // A keychain that could not be read is not written, so the token clients hold stays there.
        val token = stored ?: make().also { made -> if (problem == null) keychain { store.write(McpConstants.TOKEN_OWNER, McpConstants.TOKEN_KEY, made) } }
        kept = token
        return token
    }

    private inline fun <T> keychain(call: () -> T): T? = try {
        call()
    } catch (e: Exception) {
        problem = problem ?: e.message ?: e.toString()
        null
    }

    private fun make(): String {
        val bytes = ByteArray(BYTES).also(random::nextBytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    private companion object {
        const val BYTES = 32
    }
}

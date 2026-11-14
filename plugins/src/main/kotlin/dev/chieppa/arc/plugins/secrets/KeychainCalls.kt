package dev.chieppa.arc.plugins.secrets

import java.io.IOException

/**
 * Runs [call] on the system's keychain, turning every way it can fail into an [IOException]: a refusal already is one,
 * and a native library, symbol or call that cannot be made (an [IllegalArgumentException], [IllegalStateException] or
 * [LinkageError] from the foreign function calls) becomes one carrying the reason.
 */
internal inline fun <T> keychainCall(call: () -> T): T = try {
    call()
} catch (e: IOException) {
    throw e
} catch (e: RuntimeException) {
    throw IOException(e.message ?: e.toString(), e)
} catch (e: LinkageError) {
    throw IOException(e.message ?: e.toString(), e)
}

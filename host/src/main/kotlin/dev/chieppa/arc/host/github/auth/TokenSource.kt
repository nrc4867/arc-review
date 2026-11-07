package dev.chieppa.arc.host.github.auth

/** Where a GitHub token comes from. The token is only ever held in memory, never written or logged. */
fun interface TokenSource {
    /** The token, or null when this source has none. */
    fun token(): String?
}

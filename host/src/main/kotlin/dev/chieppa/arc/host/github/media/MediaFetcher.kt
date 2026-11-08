package dev.chieppa.arc.host.github.media

/** Fetches the picture at an address in a comment, or null when there is none to have. */
fun interface MediaFetcher {
    suspend fun fetch(url: String): ByteArray?
}

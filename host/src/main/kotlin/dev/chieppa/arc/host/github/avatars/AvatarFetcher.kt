package dev.chieppa.arc.host.github.avatars

/** Fetches the bytes of a picture at a public address, or null when there are none to have. */
fun interface AvatarFetcher {
    suspend fun fetch(url: String): ByteArray?
}

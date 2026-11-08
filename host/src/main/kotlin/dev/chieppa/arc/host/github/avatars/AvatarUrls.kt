package dev.chieppa.arc.host.github.avatars

import java.security.MessageDigest

/** Avatar addresses at the size Arc shows them, and the file name each is kept under. */
object AvatarUrls {
    /** The size in pixels asked of GitHub. */
    const val SIZE = 64

    private const val SIZE_PARAMETER = "s"
    private const val DIGEST = "SHA-256"
    private const val HEX = "%02x"

    /** [url] with its `s` query parameter set to [size], added when it has none; other parameters and any fragment are kept. */
    fun sized(url: String, size: Int = SIZE): String {
        val fragment = url.substringAfter('#', "")
        val beforeFragment = url.substringBefore('#')
        val base = beforeFragment.substringBefore('?')
        val query = beforeFragment.substringAfter('?', "")
        val kept = query.split('&').filter { it.isNotEmpty() && it.substringBefore('=') != SIZE_PARAMETER }
        val parameters = (kept + "$SIZE_PARAMETER=$size").joinToString("&")
        return "$base?$parameters" + if (fragment.isNotEmpty()) "#$fragment" else ""
    }

    /** The SHA-256 of [url] in lowercase hex. */
    fun fileName(url: String): String =
        MessageDigest.getInstance(DIGEST).digest(url.toByteArray()).joinToString("") { HEX.format(it) }
}

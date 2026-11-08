package dev.chieppa.arc.host.github.media

import java.net.URI

/** Which addresses get the person's token, and what answer counts as a picture worth keeping. */
object MediaRules {
    /** The hosts the person's token goes to: GitHub's own, where attachments to private repos are kept. */
    private val TOKEN_HOSTS = setOf("github.com", "api.github.com")

    /** The most redirects followed for one picture. */
    const val MAX_REDIRECTS = 5

    /** The largest picture kept, in bytes. */
    const val MAX_BYTES = 20 * 1024 * 1024

    private const val IMAGE = "image/"
    private const val HTTPS = "https"

    /** Whether [url] may be fetched at all: only https. */
    fun fetchable(url: String): Boolean = runCatching { URI(url).scheme == HTTPS }.getOrDefault(false)

    /** Whether the person's token goes with a request for [url]. */
    fun sendsToken(url: String): Boolean = runCatching { URI(url).host?.lowercase() in TOKEN_HOSTS }.getOrDefault(false)

    /** Whether an answer of [contentType] and [size] bytes is a picture to keep. */
    fun keeps(contentType: String?, size: Int): Boolean = contentType?.lowercase()?.startsWith(IMAGE) == true && size <= MAX_BYTES

    /** Where a redirect from [from] to [location] goes, resolved against [from]. */
    fun resolve(from: String, location: String): String = URI(from).resolve(location).toString()
}

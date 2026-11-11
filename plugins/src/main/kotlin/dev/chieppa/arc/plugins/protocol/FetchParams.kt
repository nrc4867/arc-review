package dev.chieppa.arc.plugins.protocol

import kotlinx.serialization.Serializable

/** An HTTP request: [method] to [url] with [headers] and a text [body]. Arc adds no credentials of its own. */
@Serializable
data class FetchParams(
    val url: String,
    val method: String = GET,
    val headers: Map<String, String> = emptyMap(),
    val body: String? = null,
) {
    companion object {
        const val GET = "GET"
    }
}

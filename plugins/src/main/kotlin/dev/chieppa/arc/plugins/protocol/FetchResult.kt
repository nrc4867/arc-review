package dev.chieppa.arc.plugins.protocol

import kotlinx.serialization.Serializable

/** An HTTP answer: its [status], [headers] (the first value of each) and [body] as text. Redirects are not followed. */
@Serializable
data class FetchResult(val status: Int, val headers: Map<String, String> = emptyMap(), val body: String = "")

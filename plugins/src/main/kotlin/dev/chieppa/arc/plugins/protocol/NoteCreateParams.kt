package dev.chieppa.arc.plugins.protocol

import kotlinx.serialization.Serializable

/**
 * A note or comment draft to add with [text]: on the source [source] names, else [context]'s; on [line] of [path]
 * (from [startLine] for a range) on [side], else on the source as a whole.
 */
@Serializable
data class NoteCreateParams(
    val text: String,
    val source: String? = null,
    val path: String? = null,
    val line: Int? = null,
    val startLine: Int? = null,
    val side: NoteSide? = null,
    val context: CallContext = CallContext(),
)

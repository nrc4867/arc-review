package dev.chieppa.arc.codeintel.model

import kotlinx.serialization.Serializable

/**
 * A definition's doc comment, read: the [format] it is written in (`javadoc`, `kdoc`, `jsdoc`, `sql`), its
 * [description] in Markdown, the [deprecated] note when it has one (empty for a bare tag), and its tags gathered into
 * [sections] in the order first written. It spans [firstLine] to [lastLine] of its file. Markdown links whose target
 * starts with `symbol:` name another symbol as written.
 */
@Serializable
data class HintDoc(
    val format: String,
    val description: String,
    val firstLine: Int,
    val lastLine: Int,
    val deprecated: String? = null,
    val sections: List<HintSection> = emptyList(),
)

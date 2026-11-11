package dev.chieppa.arc.syntax.docs

/**
 * A doc comment read into parts: its [format], the [description] before the first block tag as Markdown, the
 * [deprecated] tag's text when it has one (empty for a bare tag), and its other block [tags] in the order written.
 * [firstLine] and [lastLine] are the lines in the file it spans, from 1. Links to other symbols are Markdown links
 * whose target is [SYMBOL_LINK] followed by the name as written (`symbol:TokenFamily#id`).
 */
data class DocComment(
    val format: DocFormat,
    val description: String,
    val deprecated: String?,
    val tags: List<DocTag>,
    val firstLine: Int,
    val lastLine: Int,
) {
    companion object {
        /** The scheme a link to another symbol carries. */
        const val SYMBOL_LINK = "symbol:"
    }
}

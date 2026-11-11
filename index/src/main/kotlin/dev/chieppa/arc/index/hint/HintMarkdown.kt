package dev.chieppa.arc.index.hint

import dev.chieppa.arc.codeintel.model.SymbolHint

/** A hint as plain Markdown, for a reader that shows no symbol card: its signature fenced, then its description. */
internal object HintMarkdown {
    private const val FENCE = "```"

    fun of(hint: SymbolHint): String = buildString {
        append(FENCE).append(hint.target.location.document.language.orEmpty()).append('\n')
        append(hint.signature.joinToString("\n") { it.text }).append('\n')
        append(FENCE)
        hint.doc?.description?.takeIf { it.isNotBlank() }?.let { append("\n\n").append(it) }
    }
}

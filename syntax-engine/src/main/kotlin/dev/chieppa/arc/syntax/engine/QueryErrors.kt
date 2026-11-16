package dev.chieppa.arc.syntax.engine

/** The message for a query tree-sitter refused with error [type] at byte [offset] of its UTF-8 [source]. */
object QueryErrors {
    private const val SNIPPET = 60

    fun message(grammar: String, source: ByteArray, offset: Int, type: Int): String {
        val at = offset.coerceIn(0, source.size)
        val line = String(source, 0, at, Charsets.UTF_8).count { it == '\n' } + 1
        return "$grammar query error $type at line $line: ${String(source, at, minOf(SNIPPET, source.size - at), Charsets.UTF_8)}"
    }
}

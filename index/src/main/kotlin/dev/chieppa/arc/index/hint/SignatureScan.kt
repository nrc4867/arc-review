package dev.chieppa.arc.index.hint

/** Characters and words a signature scan reads specially. */
internal object SignatureScan {
    const val OPEN = "(["
    const val CLOSE = ")]"
    const val QUOTES = "\"'`"
    const val BODY = '{'
    const val END = ';'
    const val ASSIGN = '='
    const val ARROW_HEAD = '>'
    const val ESCAPE = '\\'
    const val NOT_ASSIGN_BEFORE = "=!<>"
    const val LINE_COMMENT = "//"
    const val BLOCK_COMMENT = "/*"
    const val BLOCK_COMMENT_END = "*/"
    const val SQL_BODY = "as"
    const val ELLIPSIS = "…"
}

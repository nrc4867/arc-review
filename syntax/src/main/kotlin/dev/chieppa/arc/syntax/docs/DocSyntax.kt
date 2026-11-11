package dev.chieppa.arc.syntax.docs

/** The delimiters and keywords doc comments are found and cut by. */
internal object DocSyntax {
    const val DOC_OPEN = "/**"
    const val BLOCK_OPEN = "/*"
    const val BLOCK_CLOSE = "*/"
    const val SQL_LINE = "--"
    const val FENCE = "```"
    const val BACKTICK = '`'
    const val PRE_OPEN = "<pre"
    const val PRE_CLOSE = "</pre>"

    /** Modifiers and keywords that can sit between a JavaScript doc comment and the node a definition starts at. */
    val javaScriptKeywords = setOf(
        "export", "default", "declare", "const", "let", "var", "async", "abstract", "public", "private",
        "protected", "static", "readonly", "override", "function",
    )
}

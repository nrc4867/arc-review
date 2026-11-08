package dev.chieppa.arc.syntax.runtime

/** Turns the Lua patterns some query files use with `#lua-match?` into regular expressions. */
internal object LuaPatterns {
    private val classes = mapOf(
        'a' to """\p{L}""", 'd' to """\d""", 'l' to """\p{Ll}""", 's' to """\s""", 'u' to """\p{Lu}""", 'w' to """[\p{L}\d]""",
        'x' to "[0-9A-Fa-f]", 'p' to """\p{Punct}""", 'A' to """\P{L}""", 'D' to """\D""", 'S' to """\S""", 'W' to """[^\p{L}\d]""",
    )

    fun toRegex(pattern: String): String = buildString {
        var i = 0
        while (i < pattern.length) {
            val c = pattern[i]
            if (c == '%' && i + 1 < pattern.length) {
                val next = pattern[i + 1]
                append(classes[next] ?: Regex.escape(next.toString()))
                i += 2
            } else {
                append(c)
                i++
            }
        }
    }
}

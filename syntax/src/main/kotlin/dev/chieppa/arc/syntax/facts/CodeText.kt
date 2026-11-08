package dev.chieppa.arc.syntax.facts

/** Small text helpers for what the index keeps of code. */
object CodeText {
    private val whitespace = Regex("""\s+""")

    /** [text] trimmed, with every run of whitespace one space: a type or parameter written across lines on one line. */
    fun compact(text: String): String = text.trim().replace(whitespace, " ")

    /** [text] with every whitespace character removed: a package or import path. */
    fun squeeze(text: String): String = text.replace(whitespace, "")

    /** Whether the name at [offset] in [text] follows a `.` (or `?.`), so it is looked up on some receiver. */
    fun followsDot(text: String, offset: Int): Boolean {
        var i = offset - 1
        while (i >= 0 && text[i].isWhitespace()) i--
        return i >= 0 && text[i] == '.'
    }
}

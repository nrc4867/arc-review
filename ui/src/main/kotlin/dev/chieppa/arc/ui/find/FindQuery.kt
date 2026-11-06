package dev.chieppa.arc.ui.find

/** What to find: the [text], and whether case must match, it must be a whole word, or it is a regular expression. */
data class FindQuery(
    val text: String = "",
    val matchCase: Boolean = false,
    val wholeWord: Boolean = false,
    val regex: Boolean = false,
) {
    /** Whether a plain substring search finds exactly what this asks for. */
    val plain: Boolean get() = !wholeWord && !regex
}

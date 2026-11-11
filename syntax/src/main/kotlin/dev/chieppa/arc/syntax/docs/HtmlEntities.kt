package dev.chieppa.arc.syntax.docs

/** Decodes the HTML entities JavaDoc uses. */
internal object HtmlEntities {
    val pattern = Regex("&(#[0-9]+|#[xX][0-9a-fA-F]+|[a-zA-Z]+);")

    private val named = mapOf("lt" to "<", "gt" to ">", "amp" to "&", "quot" to "\"", "apos" to "'", "nbsp" to " ")

    /** [text] with every entity this knows replaced; others stay as written. */
    fun decode(text: String): String = pattern.replace(text) { decodeOne(it) }

    /** The text for entity [match], or the match itself when unknown. */
    fun decodeOne(match: MatchResult): String {
        val body = match.groupValues[1]
        val code = when {
            body.startsWith("#x") || body.startsWith("#X") -> body.substring(2).toIntOrNull(16)
            body.startsWith("#") -> body.substring(1).toIntOrNull()
            else -> return named[body] ?: match.value
        }
        return code?.let { String(Character.toChars(it)) } ?: match.value
    }
}

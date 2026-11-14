package dev.chieppa.arc.core.activity

/**
 * Takes secrets out of text bound for the activity log: GitHub tokens by their prefixes, `Authorization` and `Bearer`
 * values, passwords in a URL's user part and token-like query parameters.
 */
object ActivityRedaction {
    const val MASK = "***"

    private val patterns = listOf(
        Regex("""\b(gh[pousr]_[A-Za-z0-9]{8,}|github_pat_[A-Za-z0-9_]{8,})"""),
        Regex("""(?i)(authorization\s*[:=]\s*)(\S+(\s+\S+)?)"""),
        Regex("""(?i)(bearer\s+)([A-Za-z0-9._~+/=-]{8,})"""),
        Regex("""(?i)((?:access_token|token|password|secret|sig)=)([^&\s]+)"""),
        Regex("""(?i)(\b[a-z][a-z0-9+.-]*://)([^/\s:@]*:[^/\s@]*@|[^/\s@]{20,}@)"""),
    )

    /** [text] with every secret replaced by [MASK]. */
    fun text(text: String): String = patterns.foldIndexed(text) { index, current, pattern ->
        pattern.replace(current) { match ->
            when (index) {
                0 -> MASK
                4 -> match.groupValues[1] + MASK + "@"
                else -> match.groupValues[1] + MASK
            }
        }
    }

    /** [entry] with its title, place and error redacted. */
    fun entry(entry: ActivityEntry): ActivityEntry = entry.copy(
        title = text(entry.title),
        place = entry.place?.let(::text),
        error = entry.error?.let(::text),
    )
}

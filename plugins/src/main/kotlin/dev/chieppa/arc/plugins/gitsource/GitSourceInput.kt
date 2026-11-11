package dev.chieppa.arc.plugins.gitsource

/**
 * Reads what a person types to add a git source: a repo's url (`https://…`, `ssh://…`, `file://…`, `git@host:path` or
 * one ending in `.git`), with the branch, tag or commit after a `#`, or the remote's default branch without one.
 */
object GitSourceInput {
    private const val REF = '#'
    private const val SCHEME = "://"
    private const val SCP_USER = "git@"
    private const val GIT_SUFFIX = ".git"

    /** The default branch of the remote, as a ref. */
    const val DEFAULT_REF = "HEAD"

    /** The url and ref [text] names, or null when it is not a git url. */
    fun parse(text: String): Pair<String, String>? {
        val trimmed = text.trim()
        val url = trimmed.substringBefore(REF).trim()
        val ref = trimmed.substringAfter(REF, "").trim().ifEmpty { DEFAULT_REF }
        val looksLikeGit = SCHEME in url || url.startsWith(SCP_USER) || url.endsWith(GIT_SUFFIX)
        return (url to ref).takeIf { looksLikeGit && url.isNotEmpty() && url.none(Char::isWhitespace) }
    }
}

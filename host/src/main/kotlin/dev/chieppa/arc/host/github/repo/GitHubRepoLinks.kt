package dev.chieppa.arc.host.github.repo

/** A GitHub repo named in typed text, and whether it came as a bare `owner/name` rather than a link. */
data class RepoLink(val repo: GitHubRepo, val bare: Boolean)

/**
 * Reads text as a GitHub repo to follow: its page (`https://github.com/owner/name`, with or without the scheme, `www.`,
 * a trailing slash or `.git`), a clone URL as [GitHubRemotes] reads it, or a bare `owner/name`. A link deeper into the
 * repo, such as a pull request's, is not a repo link.
 */
object GitHubRepoLinks {
    private const val SCHEME = "://"
    private const val HTTPS = "https://"
    private const val HOST = "github.com/"
    private const val WWW = "www."

    fun read(text: String): RepoLink? {
        val trimmed = text.trim()
        if (trimmed.isEmpty() || trimmed.any(Char::isWhitespace)) return null
        val withoutWww = trimmed.removePrefix(WWW)
        val url = when {
            SCHEME in trimmed || trimmed.contains('@') -> trimmed
            withoutWww.startsWith(HOST, ignoreCase = true) -> HTTPS + withoutWww
            else -> return GitHubRepo.parse(trimmed)?.let { RepoLink(it, bare = true) }
        }
        return GitHubRemotes.parse(url)?.let { RepoLink(it, bare = false) }
    }
}

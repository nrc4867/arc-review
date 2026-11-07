package dev.chieppa.arc.host.github.repo

/**
 * The GitHub repo a clone's remote URL points at: `https://github.com/owner/name(.git)`, `git@github.com:owner/name.git`
 * or `ssh://git@github.com/owner/name.git`. Null for any other host.
 */
object GitHubRemotes {
    private const val HOST = "github.com"
    private const val WWW = "www."
    private const val GIT_SUFFIX = ".git"
    private val SCP = Regex("^[^@/]+@([^:/]+):(.+)$")
    private val URL = Regex("^[a-zA-Z+]+://(?:[^@/]+@)?([^/:]+)(?::\\d+)?/(.+)$")

    fun parse(url: String): GitHubRepo? {
        val text = url.trim()
        val match = URL.matchEntire(text) ?: SCP.matchEntire(text) ?: return null
        val host = match.groupValues[1].lowercase().removePrefix(WWW)
        if (host != HOST) return null
        val path = match.groupValues[2].trimEnd('/').removeSuffix(GIT_SUFFIX)
        return GitHubRepo.parse(path)
    }
}

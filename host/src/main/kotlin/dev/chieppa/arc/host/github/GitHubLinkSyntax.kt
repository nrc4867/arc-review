package dev.chieppa.arc.host.github

/** The parts of a github.com link that say what it points at. */
internal object GitHubLinkSyntax {
    const val DOMAIN = "github.com"
    const val WWW_PREFIX = "www."
    const val PULL = "pull"
    const val COMMIT = "commit"
    const val COMMITS = "commits"
    const val COMPARE = "compare"
    const val RANGE = ".."
    const val BASE = "..."
    const val FORK_SEPARATOR = ':'
    val SCHEMES = setOf("https", "http")
}

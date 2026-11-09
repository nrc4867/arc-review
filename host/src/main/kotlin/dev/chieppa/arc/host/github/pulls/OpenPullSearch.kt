package dev.chieppa.arc.host.github.pulls

import dev.chieppa.arc.host.github.repo.GitHubRepo

/**
 * The search for open pull requests in some repos: one query naming every repo, which GitHub reads as any of them.
 * With text it finds those whose titles hold it; without, the most recently updated.
 */
object OpenPullSearch {
    /** The key the search goes under in a [SearchDocument]. */
    const val KEY = "open"

    private const val OPEN = "is:open"
    private const val IN_TITLE = "in:title"
    private const val RECENT = "sort:updated-desc"
    private const val REPO = "repo:"
    private val QUOTES = Regex("[\"\\\\]")

    /** The query for open pull requests in [repos] whose titles hold [text], or the latest updated for blank [text]; null when there is no repo. */
    fun query(repos: Collection<GitHubRepo>, text: String): String? {
        val words = text.replace(QUOTES, " ").trim()
        if (repos.isEmpty()) return null
        val names = repos.distinct().map { REPO + it.fullName }
        return if (words.isEmpty()) {
            (listOf(OPEN, RECENT) + names).joinToString(" ")
        } else {
            (listOf(OPEN, IN_TITLE) + names + words).joinToString(" ")
        }
    }
}

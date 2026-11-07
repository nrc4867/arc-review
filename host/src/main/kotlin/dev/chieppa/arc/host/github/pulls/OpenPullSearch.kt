package dev.chieppa.arc.host.github.pulls

import dev.chieppa.arc.host.github.repo.GitHubRepo

/**
 * The search for open pull requests in some repos whose titles hold some text: one query naming every repo, which
 * GitHub reads as any of them.
 */
object OpenPullSearch {
    /** The key the search goes under in a [SearchDocument]. */
    const val KEY = "open"

    private const val QUALIFIERS = "is:open in:title"
    private const val REPO = "repo:"
    private val QUOTES = Regex("[\"\\\\]")

    /** The query for open pull requests in [repos] whose titles hold [text]; null when there is no repo or no text. */
    fun query(repos: Collection<GitHubRepo>, text: String): String? {
        val words = text.replace(QUOTES, " ").trim()
        if (repos.isEmpty() || words.isEmpty()) return null
        return (listOf(QUALIFIERS) + repos.distinct().map { REPO + it.fullName } + words).joinToString(" ")
    }
}

package dev.chieppa.arc.ui.review

import dev.chieppa.arc.core.source.RepoName

/** Names for repos added from a folder. */
object RepoNames {
    private const val FALLBACK = "repo"
    private val NOT_ALLOWED = Regex("[^A-Za-z0-9._-]+")

    /** A repo name from a folder's name, with characters a name cannot hold replaced. */
    fun fromFolder(folder: String): RepoName {
        val cleaned = folder.replace(NOT_ALLOWED, "-").trim('-', '.')
        return RepoName.parse(cleaned) ?: RepoName(FALLBACK)
    }

    /** [base], or [base] with the first number after it that makes it unlike any of [taken]. */
    fun unique(base: RepoName, taken: Set<RepoName>): RepoName {
        if (base !in taken) return base
        return generateSequence(2) { it + 1 }.map { RepoName("${base.value}-$it") }.first { it !in taken }
    }
}

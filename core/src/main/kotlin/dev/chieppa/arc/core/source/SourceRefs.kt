package dev.chieppa.arc.core.source

import dev.chieppa.arc.core.source.SourceRefSyntax.BASE
import dev.chieppa.arc.core.source.SourceRefSyntax.BRANCH
import dev.chieppa.arc.core.source.SourceRefSyntax.COMMIT
import dev.chieppa.arc.core.source.SourceRefSyntax.PULL_REQUEST
import dev.chieppa.arc.core.source.SourceRefSyntax.RANGE

/**
 * The short text that names a source: `api#412` for a pull request, `web@3fa9c1e` for a commit,
 * `web@3fa9c1e..9c1e2fa` for a commit range, and `web:feature...main` or `web:feature` for a branch against a base.
 */
object SourceRefs {
    /** The source [text] names, or null when it names none. Surrounding whitespace is ignored. */
    fun parse(text: String): Source? {
        val ref = text.trim()
        val pr = ref.indexOf(PULL_REQUEST)
        val branch = ref.indexOf(BRANCH)
        val commit = ref.indexOf(COMMIT)
        // Repo names hold none of the marks, so the first mark in the text says which kind it is.
        val first = listOf(pr, branch, commit).filter { it > 0 }.minOrNull() ?: return null
        val repo = RepoName.parse(ref.substring(0, first)) ?: return null
        val rest = ref.substring(first + 1)
        return when (first) {
            pr -> pullRequest(repo, rest)
            branch -> branch(repo, rest)
            else -> commits(repo, rest)
        }
    }

    /** The ref text for [source], which [parse] reads back to the same source. */
    fun format(source: Source): String = when (source) {
        is Source.PullRequest -> "${source.repo}$PULL_REQUEST${source.number}"
        is Source.Commit -> "${source.repo}$COMMIT${source.sha}"
        is Source.CommitRange -> "${source.repo}$COMMIT${source.from}$RANGE${source.to}"
        is Source.BranchAgainstBase ->
            "${source.repo}$BRANCH${source.branch}" + (source.base?.let { "$BASE$it" } ?: "")
    }

    private fun pullRequest(repo: RepoName, text: String): Source? {
        if (text.isEmpty() || !text.all(Char::isDigit)) return null
        val number = text.toIntOrNull()?.takeIf { it > 0 } ?: return null
        return Source.PullRequest(repo, number)
    }

    private fun branch(repo: RepoName, text: String): Source? {
        val parts = text.split(BASE)
        return when (parts.size) {
            1 -> BranchName.parse(parts[0])?.let { Source.BranchAgainstBase(repo, it) }
            2 -> {
                val branch = BranchName.parse(parts[0]) ?: return null
                val base = BranchName.parse(parts[1]) ?: return null
                Source.BranchAgainstBase(repo, branch, base)
            }
            else -> null
        }
    }

    private fun commits(repo: RepoName, text: String): Source? {
        val parts = text.split(RANGE)
        return when (parts.size) {
            1 -> CommitSha.parse(parts[0])?.let { Source.Commit(repo, it) }
            2 -> {
                val from = CommitSha.parse(parts[0]) ?: return null
                val to = CommitSha.parse(parts[1]) ?: return null
                Source.CommitRange(repo, from, to)
            }
            else -> null
        }
    }
}

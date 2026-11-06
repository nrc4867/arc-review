package dev.chieppa.arc.core.source

/** One thing under review, in one repo. Its [ref] is the short text that names it, such as `api#412`. */
sealed interface Source {
    val repo: RepoName

    val ref: String get() = SourceRefs.format(this)

    /** A pull request, by number: `api#412`. */
    data class PullRequest(override val repo: RepoName, val number: Int) : Source {
        init {
            require(number > 0) { "A pull request number is positive: $number" }
        }
    }

    /** One commit: `web@3fa9c1e`. */
    data class Commit(override val repo: RepoName, val sha: CommitSha) : Source

    /** The commits after [from] up to and including [to], as `git log from..to` lists them: `web@3fa9c1e..9c1e2fa`. */
    data class CommitRange(override val repo: RepoName, val from: CommitSha, val to: CommitSha) : Source

    /**
     * A branch against a base, as a pull request from it would show: `web:feature...main`. Without a [base] it is
     * compared with the repo's default branch: `web:feature`.
     */
    data class BranchAgainstBase(override val repo: RepoName, val branch: BranchName, val base: BranchName? = null) : Source
}

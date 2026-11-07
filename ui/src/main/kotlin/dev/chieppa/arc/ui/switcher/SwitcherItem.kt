package dev.chieppa.arc.ui.switcher

import dev.chieppa.arc.core.change.ChangedFile
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.git.repo.LocalBranch
import dev.chieppa.arc.host.github.pulls.PullRequestSummary

/** One thing the switcher finds. */
sealed interface SwitcherItem {
    /** A source the review holds, with its [title]. */
    data class InReview(val source: Source, val title: String) : SwitcherItem

    /** A file [source]'s change touches. */
    data class Changed(val source: Source, val file: ChangedFile) : SwitcherItem

    /** A file in [source] at its head that its change does not touch. */
    data class AtHead(val source: Source, val path: String) : SwitcherItem

    /** An open pull request in a followed repo, not in the review. */
    data class FollowedPull(val source: Source.PullRequest, val pull: PullRequestSummary) : SwitcherItem

    /** A local branch of a followed repo, not in the review. */
    data class FollowedBranch(val source: Source.BranchAgainstBase, val branch: LocalBranch) : SwitcherItem
}

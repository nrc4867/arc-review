package dev.chieppa.arc.git.marks

import dev.chieppa.arc.core.change.SourceRevisions
import dev.chieppa.arc.core.mark.ReplayOutcome
import dev.chieppa.arc.core.mark.ReviewedRevisions
import dev.chieppa.arc.git.parse.MergeTreeParser
import dev.chieppa.arc.git.process.GitException
import dev.chieppa.arc.git.process.GitSettings
import dev.chieppa.arc.git.repo.BlobPrefetcher
import dev.chieppa.arc.git.repo.GitRepository

/**
 * Replays the change that was reviewed onto a source's new merge-base with one `git merge-tree`, and finds which of the
 * replayed files differ from the new head. When the merge-base has not moved, the reviewed head itself is the replay.
 */
class MarkReplayer(private val repo: GitRepository) {
    private val prefetcher = BlobPrefetcher(repo)

    /** What replaying [reviewed] onto [now] gives, or null when the reviewed commits are not in the repo. */
    suspend fun replay(reviewed: ReviewedRevisions, now: SourceRevisions): ReplayOutcome? {
        if (repo.resolveCommit(reviewed.head.value) == null) return null
        if (reviewed.mergeBase == now.base) {
            return ReplayOutcome(reviewed, emptySet(), repo.differingPaths(reviewed.head, now.head))
        }
        if (repo.resolveCommit(reviewed.mergeBase.value) == null) return null
        return try {
            prefetcher.prefetch(listOf(reviewed.mergeBase, reviewed.head, now.base))
            val args = listOf(
                "merge-tree", "--write-tree", "--name-only", "-z", "--merge-base=${reviewed.mergeBase.value}",
                now.base.value, reviewed.head.value,
            )
            val merged = MergeTreeParser.parse(repo.git(args, accept = setOf(0, GitSettings.EXIT_SOME)).stdout)
            ReplayOutcome(reviewed, merged.conflicted, repo.differingPaths(merged.tree, now.head))
        } catch (_: GitException) {
            null
        }
    }
}

package dev.chieppa.arc.git.source

import dev.chieppa.arc.core.change.ObjectId
import dev.chieppa.arc.core.change.SourceRevisions
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.git.process.GitSettings
import dev.chieppa.arc.git.repo.GitRepository

/**
 * The two commits a source compares, in its repo. A commit is compared with its first parent (the empty tree for a
 * root commit), a range with its first commit, and a branch with its merge-base with the base branch, as a pull
 * request shows it. A branch missing locally is looked for on `origin`.
 */
class SourceResolver(private val repo: GitRepository) {
    suspend fun resolve(source: Source): SourceRevisions = when (source) {
        is Source.Commit -> {
            val head = commit(source, source.sha.value)
            SourceRevisions(repo.firstParent(head) ?: ObjectId(GitSettings.EMPTY_TREE_SHA1), head)
        }
        is Source.CommitRange -> SourceRevisions(commit(source, source.from.value), commit(source, source.to.value))
        is Source.BranchAgainstBase -> {
            val head = branch(source, source.branch.value)
            val baseName = source.base?.value ?: repo.defaultBranch() ?: throw SourceNotFoundException(source)
            val base = branch(source, baseName)
            SourceRevisions(repo.mergeBase(base, head) ?: base, head)
        }
        is Source.PullRequest -> throw SourceNotFoundException(source)
    }

    private suspend fun commit(source: Source, rev: String): ObjectId =
        repo.resolveCommit(rev) ?: throw SourceNotFoundException(source)

    private suspend fun branch(source: Source, name: String): ObjectId =
        repo.resolveCommit(name) ?: repo.resolveCommit("$REMOTE/$name") ?: throw SourceNotFoundException(source)

    private companion object {
        const val REMOTE = "origin"
    }
}

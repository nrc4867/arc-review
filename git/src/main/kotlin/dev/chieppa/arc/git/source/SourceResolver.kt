package dev.chieppa.arc.git.source

import dev.chieppa.arc.core.change.ObjectId
import dev.chieppa.arc.core.change.SourceRevisions
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.git.process.GitSettings
import dev.chieppa.arc.git.repo.GitRepository

/**
 * The two commits a source compares, in its repo. A commit is compared with its first parent (the empty tree for a
 * root commit), a range with its first commit, and a branch with its merge-base with the base branch, as a pull
 * request shows it. A branch missing locally is looked for on `origin`. A pull request is a branch the host names
 * through [pulls]: its head and base are kept under private refs, `refs/arc/pull/<n>/head` and `.../base`, fetched
 * from `origin` when the clone lacks them, and it is compared from their merge-base.
 */
class SourceResolver(private val repo: GitRepository, private val pulls: PullRequestLookup = PullRequestLookup.NONE) {
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
        is Source.PullRequest -> resolve(source, pulls.refs(repo, source) ?: throw SourceNotFoundException(source))
    }

    /** The commits pull request [source] compares, where the host says its head and base are as [refs]. */
    suspend fun resolve(source: Source.PullRequest, refs: PullRequestRefs): SourceRevisions {
        val head = keep(source, PullRequestRefNames.head(source.number), refs.headOid, PullRequestRefNames.pullHead(source.number))
        val base = keep(source, PullRequestRefNames.base(source.number), refs.baseOid, PullRequestRefNames.branch(refs.baseBranch))
        return SourceRevisions(repo.mergeBase(base, head) ?: base, head)
    }

    /** [ref] at [oid] when the clone has it, else fetched from [remoteRef] on `origin`. */
    private suspend fun keep(source: Source, ref: String, oid: String?, remoteRef: String): ObjectId {
        val known = oid?.let { repo.resolveCommit(it) }
        if (known != null) {
            repo.updateRef(ref, known)
            return known
        }
        repo.fetch(listOf("+$remoteRef:$ref"))
        return repo.resolveCommit(ref) ?: throw SourceNotFoundException(source)
    }

    private suspend fun commit(source: Source, rev: String): ObjectId =
        repo.resolveCommit(rev) ?: throw SourceNotFoundException(source)

    private suspend fun branch(source: Source, name: String): ObjectId =
        repo.resolveCommit(name) ?: repo.resolveCommit("$REMOTE/$name") ?: throw SourceNotFoundException(source)

    private companion object {
        const val REMOTE = "origin"
    }
}

package dev.chieppa.arc.git.source

import dev.chieppa.arc.core.change.CommitInfo
import dev.chieppa.arc.core.change.ObjectId
import dev.chieppa.arc.core.change.SourceRevisions
import dev.chieppa.arc.core.change.SourceSnapshot
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.git.parse.PatchFingerprinter
import dev.chieppa.arc.git.process.GitSettings
import dev.chieppa.arc.git.repo.BlobPrefetcher
import dev.chieppa.arc.git.repo.GitRepository

/**
 * Reads a source from its repo: its revisions, commits, changed files and their fingerprints. The whole change is one
 * `git diff` process; a partial clone first fetches both trees' blobs in one batch. A pull request is read through
 * what [pulls] says of it, and takes its title from there.
 */
class SourceReader(private val repo: GitRepository, private val pulls: PullRequestLookup = PullRequestLookup.NONE) {
    private val resolver = SourceResolver(repo, pulls)
    private val prefetcher = BlobPrefetcher(repo)

    /** The whole of [source]. */
    suspend fun read(source: Source): SourceSnapshot {
        val pull = (source as? Source.PullRequest)?.let { pulls.refs(repo, it) ?: throw SourceNotFoundException(it) }
        val revisions = if (pull != null) resolver.resolve(source as Source.PullRequest, pull) else resolver.resolve(source)
        val commits = when (source) {
            is Source.Commit -> listOfNotNull(repo.commit(revisions.head))
            else -> repo.commits(revisions.base.takeUnless(::isEmptyTree), revisions.head)
        }
        val title = pull?.title ?: commits.lastOrNull()?.subject ?: repo.commit(revisions.head)?.subject.orEmpty()
        return snapshot(source, revisions, title, commits)
    }

    /** One [commit] of [source], compared with its first parent. */
    suspend fun readCommit(source: Source, commit: ObjectId): SourceSnapshot {
        val info = repo.commit(commit) ?: throw SourceNotFoundException(source)
        val revisions = SourceRevisions(repo.firstParent(commit) ?: ObjectId(GitSettings.EMPTY_TREE_SHA1), commit)
        return snapshot(source, revisions, info.subject, listOf(info))
    }

    private suspend fun snapshot(source: Source, revisions: SourceRevisions, title: String, commits: List<CommitInfo>): SourceSnapshot {
        prefetcher.prefetch(listOf(revisions.base, revisions.head).filterNot(::isEmptyTree))
        val outputs = repo.diff(revisions.base, revisions.head)
        return SourceSnapshot(
            source = source,
            revisions = revisions,
            title = title,
            commits = commits,
            files = outputs.map { it.file }.sortedBy { it.path },
            fingerprints = outputs.associate { it.file.path to PatchFingerprinter.fingerprint(it.patch) },
        )
    }

    private fun isEmptyTree(id: ObjectId): Boolean = id.value == GitSettings.EMPTY_TREE_SHA1
}

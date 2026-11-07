package dev.chieppa.arc.git.revision

import dev.chieppa.arc.core.change.CommitInfo
import dev.chieppa.arc.core.change.ObjectId
import dev.chieppa.arc.core.change.SourceRevisions
import dev.chieppa.arc.core.mark.PatchFingerprint
import dev.chieppa.arc.core.revision.PatchChanges
import dev.chieppa.arc.core.revision.RecordedHead
import dev.chieppa.arc.core.revision.RevisionHistory
import dev.chieppa.arc.core.revision.SourceRevision
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.git.parse.PatchFingerprinter
import dev.chieppa.arc.git.process.GitSettings
import dev.chieppa.arc.git.repo.BlobPrefetcher
import dev.chieppa.arc.git.repo.GitRepository
import dev.chieppa.arc.git.source.SourceResolver

/**
 * Reads the revisions a source has had: the heads Arc recorded when reading it, and for a pull request the heads the
 * host knows through [pushes]. Every head the clone has, or can fetch from `origin` by sha, is pinned and read; the
 * rest are left out. A head's base is the one recorded with it, else its merge-base with the base branch's tip now.
 * Revisions run oldest first by their head commit's time; each one's change is one `git diff` from its base.
 */
class RevisionReader(
    private val repo: GitRepository,
    private val resolver: SourceResolver = SourceResolver(repo),
    private val pushes: PushHistoryLookup = PushHistoryLookup.NONE,
) {
    private val pins = HeadPins(repo)
    private val prefetcher = BlobPrefetcher(repo)

    /** The revisions of [source] from its [recorded] heads and its host's. */
    suspend fun read(source: Source, recorded: List<RecordedHead>): RevisionHistory {
        val pushed = (source as? Source.PullRequest)?.let { pushes.heads(repo, it) }.orEmpty()
        val bases = recorded.associate { it.head.value to it.base }
        val pushedAt = pushed.mapNotNull { head -> head.pushedAt?.let { head.oid to it } }.toMap()
        val kept = pins.keep(recorded.map { it.head.value } + pushed.map { it.oid })
        val tip = resolver.baseTip(source)
        val heads = kept.mapNotNull { (oid, head) ->
            val base = bases[oid] ?: tip?.let { repo.mergeBase(head, it) } ?: return@mapNotNull null
            val made = repo.commit(head)?.committed ?: return@mapNotNull null
            Head(head, base, pushedAt[oid] ?: made, made)
        }.distinctBy { it.head }.sortedBy { it.made }
        val revisions = mutableListOf<SourceRevision>()
        var before: Pair<Head, List<CommitInfo>>? = null
        for ((i, head) in heads.withIndex()) {
            val commits = commits(source, head)
            val previous = before?.first
            val rewrote = previous != null && !repo.isAncestor(previous.head, head.head)
            val known = before?.second.orEmpty().map { it.id }.toSet()
            revisions += SourceRevision(
                number = i + 1,
                head = head.head,
                base = head.base,
                at = head.at,
                rewrote = rewrote,
                rebased = rewrote && previous?.base != head.base,
                commits = commits.filter { it.id !in known },
                fingerprints = fingerprints(SourceRevisions(head.base, head.head)),
                pinned = true,
            )
            before = head to commits
        }
        return RevisionHistory(source, revisions)
    }

    /** The paths whose change differs between [a] and [b], each taken from its own base, so a rebase alone changes nothing. */
    suspend fun differing(a: SourceRevisions, b: SourceRevisions): Set<String> = PatchChanges.differing(fingerprints(a), fingerprints(b))

    private suspend fun commits(source: Source, head: Head): List<CommitInfo> = when (source) {
        is Source.Commit -> listOfNotNull(repo.commit(head.head))
        else -> repo.commits(head.base.takeUnless(::isEmptyTree), head.head)
    }

    private suspend fun fingerprints(revisions: SourceRevisions): Map<String, PatchFingerprint> {
        prefetcher.prefetch(listOf(revisions.base, revisions.head).filterNot(::isEmptyTree))
        return repo.diff(revisions.base, revisions.head).associate { it.file.path to PatchFingerprinter.fingerprint(it.patch) }
    }

    private fun isEmptyTree(id: ObjectId): Boolean = id.value == GitSettings.EMPTY_TREE_SHA1

    /** A head to read: its commit and base, when it became the head as far as is known, and when it was made. */
    private data class Head(val head: ObjectId, val base: ObjectId, val at: Long, val made: Long)
}

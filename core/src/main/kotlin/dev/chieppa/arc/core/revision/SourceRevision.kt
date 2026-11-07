package dev.chieppa.arc.core.revision

import dev.chieppa.arc.core.change.CommitInfo
import dev.chieppa.arc.core.change.ObjectId
import dev.chieppa.arc.core.mark.PatchFingerprint

/**
 * One head a source has had, numbered from 1 oldest first: its change from [base] to [head], [at] when it became the
 * head as far as is known (epoch seconds), whether it [rewrote] the revision before it, as a force push does, and
 * whether it was [rebased] (rewritten onto a base that moved). [commits] are those it brought that the revision
 * before did not have, oldest first; [fingerprints] are its changed files' fingerprints by path. [pinned] says Arc
 * keeps its head under a private ref.
 */
data class SourceRevision(
    val number: Int,
    val head: ObjectId,
    val base: ObjectId,
    val at: Long,
    val rewrote: Boolean = false,
    val rebased: Boolean = false,
    val commits: List<CommitInfo> = emptyList(),
    val fingerprints: Map<String, PatchFingerprint> = emptyMap(),
    val pinned: Boolean = false,
)

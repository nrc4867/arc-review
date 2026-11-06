package dev.chieppa.arc.store.review

import dev.chieppa.arc.core.change.ObjectId
import dev.chieppa.arc.core.mark.FileMark
import dev.chieppa.arc.core.mark.MarkKind
import dev.chieppa.arc.core.mark.PatchFingerprint
import dev.chieppa.arc.core.mark.PatchHash
import dev.chieppa.arc.core.mark.ReviewedRevisions
import dev.chieppa.arc.core.pane.Pane
import dev.chieppa.arc.core.pane.PaneId
import dev.chieppa.arc.core.pane.PaneView
import dev.chieppa.arc.core.pane.Revision
import dev.chieppa.arc.core.review.Review
import dev.chieppa.arc.core.review.ReviewId
import dev.chieppa.arc.core.source.RepoName
import dev.chieppa.arc.core.source.SourceRefs

/** Turns a [Review] into what the store writes and back. A stored entry that does not parse is left out. */
object ReviewMapping {
    fun toStored(review: Review, views: List<StoredSourceView> = emptyList(), paneStates: Map<String, StoredPaneState> = emptyMap()) = StoredReview(
        id = review.id.value,
        name = review.name,
        sources = review.sources.map { it.ref },
        active = review.active?.ref,
        panes = review.panes.map(::toStored),
        marks = review.marks.map(::toStored),
        views = views,
        paneStates = paneStates,
    )

    fun toReview(stored: StoredReview): Review {
        val sources = stored.sources.mapNotNull(SourceRefs::parse).distinct()
        val active = stored.active?.let(SourceRefs::parse)?.takeIf { it in sources } ?: sources.firstOrNull()
        return Review(
            id = ReviewId(stored.id),
            name = stored.name,
            sources = sources,
            active = active,
            panes = stored.panes.mapNotNull(::toPane).distinctBy { it.id },
            marks = stored.marks.mapNotNull(::toMark).distinctBy { it.source to it.path },
        )
    }

    private fun toStored(pane: Pane) = StoredPane(
        id = pane.id.value,
        repo = pane.repo.value,
        path = pane.path,
        revision = pane.revision.value,
        view = pane.view.name,
        pinned = pane.pinned,
        base = pane.base?.value,
        oldPath = pane.oldPath,
        source = pane.source?.ref,
    )

    private fun toStored(mark: FileMark) = StoredMark(
        source = mark.source.ref,
        path = mark.path,
        kind = mark.kind.name,
        meta = mark.fingerprint.meta.value,
        exact = mark.fingerprint.exact.value,
        hunks = mark.fingerprint.hunks.map { it.value },
        reviewedHead = mark.reviewed?.head?.value,
        reviewedBase = mark.reviewed?.mergeBase?.value,
    )

    private fun toPane(stored: StoredPane): Pane? = readOrNull {
        Pane(
            id = PaneId(stored.id),
            repo = RepoName(stored.repo),
            path = stored.path,
            revision = Revision(stored.revision),
            view = PaneView.valueOf(stored.view),
            pinned = stored.pinned,
            base = stored.base?.let(::Revision),
            oldPath = stored.oldPath,
            source = stored.source?.let(SourceRefs::parse),
        )
    }

    private fun toMark(stored: StoredMark): FileMark? = readOrNull {
        val head = stored.reviewedHead
        val base = stored.reviewedBase
        FileMark(
            source = SourceRefs.parse(stored.source) ?: return@readOrNull null,
            path = stored.path,
            fingerprint = PatchFingerprint(PatchHash(stored.meta), PatchHash(stored.exact), stored.hunks.map(::PatchHash)),
            kind = MarkKind.valueOf(stored.kind),
            reviewed = if (head != null && base != null) ReviewedRevisions(ObjectId(head), ObjectId(base)) else null,
        )
    }

    private inline fun <T> readOrNull(read: () -> T?): T? = try {
        read()
    } catch (_: IllegalArgumentException) {
        null
    }
}

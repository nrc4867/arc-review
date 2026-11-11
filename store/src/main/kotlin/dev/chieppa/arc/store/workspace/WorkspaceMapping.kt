package dev.chieppa.arc.store.workspace

import dev.chieppa.arc.core.change.ObjectId
import dev.chieppa.arc.core.mark.FileMark
import dev.chieppa.arc.core.mark.MarkKind
import dev.chieppa.arc.core.mark.PatchFingerprint
import dev.chieppa.arc.core.mark.PatchHash
import dev.chieppa.arc.core.mark.ReviewedRevisions
import dev.chieppa.arc.core.actions.RunRef
import dev.chieppa.arc.core.pane.Page
import dev.chieppa.arc.core.pane.Pane
import dev.chieppa.arc.core.pane.PaneId
import dev.chieppa.arc.core.pane.PaneView
import dev.chieppa.arc.core.pane.Revision
import dev.chieppa.arc.core.workspace.Workspace
import dev.chieppa.arc.core.source.RepoName
import dev.chieppa.arc.core.revision.RecordedHead
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.core.source.SourceRefs

/** Turns a [Workspace] into what the store writes and back. A stored entry that does not parse is left out. */
object WorkspaceMapping {
    fun toStored(workspace: Workspace, views: List<StoredSourceView> = emptyList(), paneStates: Map<String, StoredPaneState> = emptyMap()) = StoredWorkspace(
        sources = workspace.sources.map { it.ref },
        active = workspace.active?.ref,
        panes = workspace.panes.map(::toStored),
        marks = workspace.marks.map(::toStored),
        views = views,
        paneStates = paneStates,
        pages = workspace.pages.map(::toStored),
        heads = workspace.heads.map { (source, heads) -> StoredHeads(source.ref, heads.map { StoredHead(it.head.value, it.base.value, it.at) }) },
    )

    fun toWorkspace(stored: StoredWorkspace): Workspace {
        val sources = stored.sources.mapNotNull(SourceRefs::parse).distinct()
        val active = stored.active?.let(SourceRefs::parse)?.takeIf { it in sources } ?: sources.firstOrNull()
        val panes = stored.panes.mapNotNull(::toPane).distinctBy { it.id }
        val paneIds = panes.map { it.id }.toSet()
        return Workspace(
            sources = sources,
            active = active,
            panes = panes,
            marks = stored.marks.mapNotNull(::toMark).distinctBy { it.source to it.path },
            pages = stored.pages.mapNotNull(::toPage).distinctBy { it.id }.filter { it.id !in paneIds },
            heads = stored.heads.mapNotNull(::toHeads).toMap(),
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

    private fun toStored(page: Page): StoredPage = when (page) {
        is Page.Filter -> StoredPage.Filter(page.id.value, page.filter)
        is Page.RevisionMatrix -> StoredPage.RevisionMatrix(page.id.value, page.source.ref, page.pinned)
        is Page.Conversation -> StoredPage.Conversation(page.id.value, page.source.ref, page.pinned)
        is Page.Runs -> StoredPage.Runs(page.id.value, page.filter)
        is Page.Run -> StoredPage.Run(page.id.value, page.run.repo, page.run.id)
        is Page.Settings -> StoredPage.Settings(page.id.value, page.section)
    }

    private fun toPage(stored: StoredPage): Page? = readOrNull {
        when (stored) {
            is StoredPage.Filter -> Page.Filter(PaneId(stored.id), stored.filter)
            is StoredPage.RevisionMatrix -> SourceRefs.parse(stored.source)?.let { Page.RevisionMatrix(PaneId(stored.id), it, stored.pinned) }
            is StoredPage.Conversation -> SourceRefs.parse(stored.source)?.let { Page.Conversation(PaneId(stored.id), it, stored.pinned) }
            is StoredPage.Runs -> Page.Runs(PaneId(stored.id), stored.filter)
            is StoredPage.Run -> Page.Run(PaneId(stored.id), RunRef(stored.repo, stored.run))
            is StoredPage.Settings -> Page.Settings(PaneId(stored.id), stored.section)
        }
    }

    private fun toHeads(stored: StoredHeads): Pair<Source, List<RecordedHead>>? {
        val source = SourceRefs.parse(stored.source) ?: return null
        val heads = stored.heads.mapNotNull { head -> readOrNull { RecordedHead(ObjectId(head.head), ObjectId(head.base), head.at) } }
        return source to heads.distinctBy { it.head }
    }

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

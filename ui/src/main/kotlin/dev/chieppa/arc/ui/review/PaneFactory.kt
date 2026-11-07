package dev.chieppa.arc.ui.review

import dev.chieppa.arc.core.change.ChangedFile
import dev.chieppa.arc.core.change.SourceSnapshot
import dev.chieppa.arc.core.pane.Pane
import dev.chieppa.arc.core.pane.PaneId
import dev.chieppa.arc.core.pane.PaneView
import dev.chieppa.arc.core.pane.Revision
import dev.chieppa.arc.core.review.Review
import dev.chieppa.arc.core.source.RepoName
import dev.chieppa.arc.core.source.Source

/** Makes panes: a changed file of a source as a diff, or any file at any revision on its own. */
object PaneFactory {
    private const val PREFIX = "p"
    private const val MARKDOWN = ".md"
    private const val SVG = ".svg"

    /** [file]'s change in [snapshot], shown as a diff. */
    fun forChange(id: PaneId, snapshot: SourceSnapshot, file: ChangedFile): Pane = Pane(
        id = id,
        repo = snapshot.source.repo,
        path = file.path,
        revision = Revision(snapshot.revisions.head.value),
        view = PaneView.Diff,
        base = Revision(snapshot.revisions.base.value),
        oldPath = file.oldPath,
        source = snapshot.source,
    )

    /** [path] in [repo] at [revision], whole: Markdown and SVG rendered, anything else as its text. */
    fun forFile(id: PaneId, repo: RepoName, revision: Revision, path: String, source: Source? = null): Pane =
        Pane(id, repo, path, revision, if (rendersFirst(path)) PaneView.Rendered else PaneView.FullFile, source = source)

    fun isMarkdown(path: String): Boolean = path.lowercase().endsWith(MARKDOWN)

    fun isSvg(path: String): Boolean = path.lowercase().endsWith(SVG)

    /** Whether a file at [path] has a rendered view, which a pane opened on it alone shows first. */
    fun rendersFirst(path: String): Boolean = isMarkdown(path) || isSvg(path)

    /** An id no pane in [review] has yet. */
    fun nextId(review: Review): PaneId {
        val taken = review.panes.mapNotNull { it.id.value.removePrefix(PREFIX).toIntOrNull() }
        return PaneId(PREFIX + ((taken.maxOrNull() ?: 0) + 1))
    }
}

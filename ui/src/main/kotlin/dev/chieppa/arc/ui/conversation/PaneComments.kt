package dev.chieppa.arc.ui.conversation

import dev.chieppa.arc.core.change.ObjectId
import dev.chieppa.arc.core.conversation.CommentAnchor
import dev.chieppa.arc.core.conversation.LineSide
import dev.chieppa.arc.core.pane.Pane
import dev.chieppa.arc.ui.workbench.Workbench

/** Starting the person's comments on the lines a pane shows. */
object PaneComments {
    private val SHA = Regex("[0-9a-f]{40}|[0-9a-f]{64}")

    /**
     * Starts a comment on the lines from [start] to [end] of [side] in [pane]'s file, written at the revision the pane
     * shows when it names a commit, else at its source's head. Nothing starts for a pane with no source.
     */
    fun start(workbench: Workbench, pane: Pane, side: LineSide, start: Int, end: Int = start) {
        val source = pane.source ?: return
        val shown = pane.revision.value.takeIf { SHA.matches(it) }?.let(::ObjectId)
        val madeOn = shown ?: workbench.state.snapshot(source)?.revisions?.head
        val first = minOf(start, end)
        val last = maxOf(start, end)
        workbench.comments.controller.startComment(source, CommentAnchor(pane.path, last, side, first.takeIf { it != last }, madeOn))
    }
}

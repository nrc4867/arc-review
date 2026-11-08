package dev.chieppa.arc.ui.conversation

import dev.chieppa.arc.core.conversation.LineSide
import dev.chieppa.arc.core.conversation.LocalThreads
import dev.chieppa.arc.core.conversation.ReviewThread
import dev.chieppa.arc.core.conversation.ThreadState
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.ui.workbench.Workbench

/** Going from a list of comments to a thread under its line, opened. */
object ThreadJumps {
    /** Opens [path] of [source]'s change, unfolds thread [id] in [state] and scrolls its [line] on [side] into view. */
    fun to(workbench: Workbench, source: Source, path: String, side: LineSide, line: Int, id: String, state: ThreadState): Boolean {
        if (!workbench.panes.openChange(source, path)) return false
        workbench.threadViews.open(id, state)
        workbench.threadViews.reveal = ThreadReveal(source, path, side, line)
        return true
    }

    /** Opens [thread] under its line. */
    fun to(workbench: Workbench, thread: ReviewThread): Boolean =
        to(workbench, thread.source, thread.path, thread.side, thread.line, thread.id, thread.state)

    /**
     * Opens the person's comment [id] under [line], where it sits now, or under its own line when [line] is null; a
     * reply opens the thread it answers.
     */
    fun toLocal(workbench: Workbench, id: String, line: Int? = null): Boolean {
        val controller = workbench.comments.controller
        val comment = controller.local.byId(id) ?: return false
        val anchor = comment.anchor ?: return false
        val answered = comment.inReplyTo
        if (answered != null) {
            val thread = controller.hosted[comment.source]?.conversation?.threads?.firstOrNull { it.id == answered } ?: return false
            return to(workbench, thread)
        }
        return to(workbench, comment.source, anchor.path, anchor.side, line ?: anchor.line, id, LocalThreads.stateOf(comment.kind))
    }
}

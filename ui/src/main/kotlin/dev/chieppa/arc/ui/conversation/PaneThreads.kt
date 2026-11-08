package dev.chieppa.arc.ui.conversation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import dev.chieppa.arc.core.conversation.LocalThreads
import dev.chieppa.arc.core.conversation.ReviewThread
import dev.chieppa.arc.core.conversation.ThreadContext
import dev.chieppa.arc.core.pane.Pane
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.comment_you
import dev.chieppa.arc.ui.workbench.Workbench
import org.jetbrains.compose.resources.stringResource

/**
 * The threads to show on [pane]'s file: the host's, with the person's replies, any sample threads placed against the
 * lines [changed] and the file's [lineCount], and the person's own comments and notes where their lines are now.
 */
@Composable
fun paneThreads(workbench: Workbench, pane: Pane, changed: List<Int>, lineCount: Int): List<ReviewThread> {
    val source = pane.source ?: return emptyList()
    val samples = remember(pane.id, pane.path, pane.revision, changed, lineCount) {
        workbench.conversation.threads(source, pane.path, ThreadContext(changed, lineCount))
    }
    val tools = workbench.comments
    val local = tools.controller.local
    val snapshot = workbench.state.snapshot(source)
    val you = stringResource(Res.string.comment_you)
    val host = tools.controller.hostThreads(source, pane.path)
    val own = LocalThreads.on(local, source, pane.path, you) { tools.placer.lineOf(it, snapshot) }
    return workbench.threadViews.shown(LocalThreads.withReplies(host + samples, local)) + own
}

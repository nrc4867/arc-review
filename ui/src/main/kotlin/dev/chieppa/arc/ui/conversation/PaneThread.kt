package dev.chieppa.arc.ui.conversation

import androidx.compose.runtime.Composable
import dev.chieppa.arc.core.conversation.ReviewThread
import dev.chieppa.arc.core.conversation.ThreadState
import dev.chieppa.arc.ui.workbench.Workbench

/**
 * One thread under its line in a pane: the person's draft or note as a card they write in, a host's or guest's
 * thread as a card of messages. A staged comment shows only its gutter mark until it is opened.
 */
@Composable
fun PaneThread(thread: ReviewThread, workbench: Workbench) {
    when (thread.state) {
        ThreadState.Staged -> if (workbench.threadViews.expanded(thread)) LocalCard(thread, workbench)
        ThreadState.Draft, ThreadState.Note -> LocalCard(thread, workbench)
        else -> ThreadCard(thread, workbench)
    }
}

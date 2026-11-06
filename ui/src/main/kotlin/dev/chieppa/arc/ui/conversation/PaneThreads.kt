package dev.chieppa.arc.ui.conversation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import dev.chieppa.arc.core.conversation.ReviewThread
import dev.chieppa.arc.core.conversation.ThreadContext
import dev.chieppa.arc.core.pane.Pane
import dev.chieppa.arc.ui.workbench.Workbench

/** The threads to show on [pane]'s file, placed against the lines [changed] and the file's [lineCount]. */
@Composable
fun paneThreads(workbench: Workbench, pane: Pane, changed: List<Int>, lineCount: Int): List<ReviewThread> {
    val source = pane.source ?: return emptyList()
    val all = remember(pane.id, pane.path, pane.revision, changed, lineCount) {
        workbench.conversation.threads(source, pane.path, ThreadContext(changed, lineCount))
    }
    return workbench.threadViews.shown(all)
}

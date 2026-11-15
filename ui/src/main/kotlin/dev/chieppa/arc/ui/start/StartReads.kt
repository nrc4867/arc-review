package dev.chieppa.arc.ui.start

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.ui.workbench.Workbench

/**
 * Reads what the start screen shows of each source in [selection] and is not read yet: its revisions once it is read, again
 * at each new head, and its conversation on the host.
 */
@Composable
fun StartReads(workbench: Workbench, selection: List<Source>) {
    val state = workbench.state
    val controller = workbench.comments.controller
    for (source in selection) {
        key(source) {
            val head = state.snapshot(source)?.revisions?.head
            LaunchedEffect(head) { if (head != null) workbench.revisions.load(source) }
            LaunchedEffect(Unit) { if (controller.hosted[source] == null) controller.load(source) }
        }
    }
}

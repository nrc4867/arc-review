package dev.chieppa.arc.ui.conversation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import dev.chieppa.arc.core.conversation.StagedComment
import dev.chieppa.arc.ui.workbench.Workbench

/** The staged comments on the workspace's sources, placed against the files each one changes. */
@Composable
fun stagedComments(workbench: Workbench): List<StagedComment> {
    val state = workbench.state
    val changes = state.workspace.sources.associateWith { source -> state.snapshot(source)?.files.orEmpty().map { it.path } }
    return remember(changes) { workbench.conversation.staged(changes) }
}

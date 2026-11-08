package dev.chieppa.arc.ui.conversation

import androidx.compose.runtime.Composable
import dev.chieppa.arc.core.conversation.StagedComment
import dev.chieppa.arc.ui.workbench.Workbench

/** The staged comments on every source in the workspace, placed against each source as it is now. */
@Composable
fun stagedComments(workbench: Workbench): List<StagedComment> =
    workbench.state.workspace.sources.flatMap { workbench.comments.pusher.staged(it) }

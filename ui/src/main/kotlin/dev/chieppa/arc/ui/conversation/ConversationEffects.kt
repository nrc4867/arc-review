package dev.chieppa.arc.ui.conversation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import dev.chieppa.arc.ui.workbench.Workbench
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Keeps the conversation in step with the workspace: a pull request's conversation is read from the host when it
 * becomes the active source, and the person's comments on each source are placed again whenever the source is read
 * at a new head or the comments change.
 */
@Composable
fun ConversationEffects(workbench: Workbench) {
    val state = workbench.state
    val tools = workbench.comments
    LaunchedEffect(workbench) {
        snapshotFlow { state.active }.distinctUntilChanged().collect { source -> source?.let(tools.controller::load) }
    }
    LaunchedEffect(workbench) {
        snapshotFlow {
            state.workspace.sources.mapNotNull { source ->
                val snapshot = state.snapshot(source) ?: return@mapNotNull null
                snapshot to tools.controller.local.of(source).filter { it.anchor != null }
            }
        }.distinctUntilChanged().collectLatest { placing ->
            for ((snapshot, comments) in placing) tools.placer.update(snapshot, comments)
        }
    }
}

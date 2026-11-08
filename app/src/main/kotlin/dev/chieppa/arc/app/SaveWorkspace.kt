package dev.chieppa.arc.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import dev.chieppa.arc.ui.workbench.Workbench
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.withContext

/**
 * Hands [workbench]'s workspace to [save] a moment after each change to it: its sources, panes, marks, each source's
 * view, the panes' own state, the repos and the open tree rows.
 */
@OptIn(FlowPreview::class)
@Composable
fun SaveWorkspace(workbench: Workbench, save: (Workbench) -> Unit) {
    val current by rememberUpdatedState(save)
    LaunchedEffect(workbench) {
        val state = workbench.state
        snapshotFlow { listOf(state.workspace, state.views, state.paneStates, state.repos, workbench.treeState.expanded) }
            .drop(1)
            .debounce(AppConstants.SAVE_DELAY_MILLIS)
            .collect { withContext(workbench.dispatchers.io) { current(workbench) } }
    }
}

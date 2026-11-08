package dev.chieppa.arc.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import dev.chieppa.arc.core.recent.RecentSources
import dev.chieppa.arc.core.start.StartLayout
import dev.chieppa.arc.ui.workbench.Workbench
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Hands the person's layout of the start screens to [saveLayout], and the recently opened sources to [saveRecent], on
 * [Workbench.dispatchers]' io a moment after each change to them.
 */
@OptIn(FlowPreview::class)
@Composable
fun SaveStart(workbench: Workbench, saveLayout: (StartLayout) -> Unit, saveRecent: (RecentSources) -> Unit) {
    val layout by rememberUpdatedState(saveLayout)
    val recent by rememberUpdatedState(saveRecent)
    LaunchedEffect(workbench) {
        val start = workbench.start
        launch {
            snapshotFlow { start.layout }
                .drop(1)
                .debounce(AppConstants.SAVE_DELAY_MILLIS)
                .collect { withContext(workbench.dispatchers.io) { layout(it) } }
        }
        snapshotFlow { start.recent }
            .drop(1)
            .debounce(AppConstants.SAVE_DELAY_MILLIS)
            .collect { withContext(workbench.dispatchers.io) { recent(it) } }
    }
}

package dev.chieppa.arc.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import dev.chieppa.arc.core.actions.RunWatchlist
import dev.chieppa.arc.core.actions.timing.TimingHistory
import dev.chieppa.arc.ui.runs.RunWatch
import dev.chieppa.arc.ui.util.ArcDispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Hands [watch]'s run filters to [saveWatchlist], and its step timings to [saveTimings], on [dispatchers]' io a moment after each change. */
@OptIn(FlowPreview::class)
@Composable
fun SaveRuns(watch: RunWatch, dispatchers: ArcDispatchers, saveWatchlist: (RunWatchlist) -> Unit, saveTimings: (TimingHistory) -> Unit) {
    val watchlist by rememberUpdatedState(saveWatchlist)
    val timings by rememberUpdatedState(saveTimings)
    LaunchedEffect(watch) {
        launch {
            snapshotFlow { watch.watchlist }.drop(1).debounce(AppConstants.SAVE_DELAY_MILLIS).collect { withContext(dispatchers.io) { watchlist(it) } }
        }
        snapshotFlow { watch.timings }.drop(1).debounce(AppConstants.SAVE_DELAY_MILLIS).collect { withContext(dispatchers.io) { timings(it) } }
    }
}

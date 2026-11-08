package dev.chieppa.arc.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import dev.chieppa.arc.core.watch.Watchlist
import dev.chieppa.arc.ui.pulls.PullWatch
import dev.chieppa.arc.ui.util.ArcDispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.withContext

/** Hands [watch]'s saved filters and rules to [save] on [dispatchers]' io a moment after each change to them. */
@OptIn(FlowPreview::class)
@Composable
fun SaveWatchlist(watch: PullWatch, dispatchers: ArcDispatchers, save: (Watchlist) -> Unit) {
    val current by rememberUpdatedState(save)
    LaunchedEffect(watch) {
        snapshotFlow { watch.watchlist }
            .drop(1)
            .debounce(AppConstants.SAVE_DELAY_MILLIS)
            .collect { withContext(dispatchers.io) { current(it) } }
    }
}

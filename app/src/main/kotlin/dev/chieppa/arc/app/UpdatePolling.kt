package dev.chieppa.arc.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import dev.chieppa.arc.ui.workbench.Workbench
import kotlinx.coroutines.delay

/** Asks for a newer release of Arc when the window opens and every day while it stays open. */
@Composable
fun UpdatePolling(workbench: Workbench) {
    if (!workbench.updates.available) return
    LaunchedEffect(workbench) {
        while (true) {
            workbench.updates.check()
            delay(AppConstants.UPDATE_CHECK_MILLIS)
        }
    }
}

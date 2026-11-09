package dev.chieppa.arc.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import dev.chieppa.arc.ui.workbench.Workbench

/** Polls GitHub Actions for [workbench]'s runs while the window is open. */
@Composable
fun RunPolling(workbench: Workbench) {
    LaunchedEffect(workbench) { workbench.runPoller.run() }
}

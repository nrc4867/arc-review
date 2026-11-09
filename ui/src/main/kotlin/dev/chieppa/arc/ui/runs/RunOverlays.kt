package dev.chieppa.arc.ui.runs

import androidx.compose.runtime.Composable

/** The run dialogs over the window: Run workflow, and the confirmation of a write to a run. */
@Composable
fun RunOverlays(controller: RunController) {
    RunWorkflowDialog(controller)
    RunConfirmDialog(controller)
}

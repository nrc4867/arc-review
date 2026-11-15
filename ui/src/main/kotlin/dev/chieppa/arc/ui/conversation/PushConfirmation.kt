package dev.chieppa.arc.ui.conversation

import androidx.compose.runtime.Composable
import dev.chieppa.arc.ui.workbench.Workbench

/** The push confirmation over the window while a source's push waits on it, wherever the push was asked for. */
@Composable
fun PushConfirmation(workbench: Workbench) {
    workbench.comments.pusher.confirming?.let { PushDialog(workbench, it) }
}

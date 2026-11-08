package dev.chieppa.arc.ui.conversation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.ui.Divider

/**
 * The Staged comments drawer for the active source: its verdict and summary, one row per staged comment, and the push
 * at the foot, which asks for confirmation first. Plain while there is no source.
 */
@Composable
fun StagedCommentsContent(workbench: Workbench) {
    val source = workbench.state.active ?: return
    val pusher = workbench.comments.pusher
    val staged = pusher.staged(source)
    Column(Modifier.fillMaxSize()) {
        StagedHead(workbench, source)
        Divider()
        Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())) {
            for (comment in staged) {
                StagedRow(comment, workbench)
                Divider()
            }
        }
        Divider()
        StagedFooter(workbench, source, staged)
    }
    if (pusher.confirming == source) PushDialog(workbench, source)
}

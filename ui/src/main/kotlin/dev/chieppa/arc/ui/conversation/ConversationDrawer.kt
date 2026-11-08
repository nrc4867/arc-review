package dev.chieppa.arc.ui.conversation

import androidx.compose.runtime.Composable
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.ui.workbench.Workbench

/** The Conversation drawer for the active pull request; plain for a source that is not a pull request. */
@Composable
fun ConversationDrawer(workbench: Workbench) {
    val source = workbench.state.active as? Source.PullRequest ?: return
    ConversationPanel(workbench, source, inTab = false)
}

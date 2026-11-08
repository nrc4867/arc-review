package dev.chieppa.arc.ui.conversation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.chieppa.arc.core.pane.Page
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.ui.util.ArcSizes
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.theme.Stretch

/** A pull request's conversation as a tab in the grid, read from the host when it opens, in a column that reads well wide. */
@Composable
fun ConversationPage(page: Page.Conversation, workbench: Workbench, modifier: Modifier = Modifier) {
    val source = page.source as? Source.PullRequest ?: return
    val controller = workbench.comments.controller
    LaunchedEffect(source) { if (controller.hosted[source] == null) controller.load(source) }
    Box(modifier.fillMaxSize().background(Stretch.colors.surfacePanel), contentAlignment = Alignment.TopCenter) {
        ConversationPanel(workbench, source, inTab = true, Modifier.widthIn(max = ArcSizes.conversationPage))
    }
}

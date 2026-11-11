package dev.chieppa.arc.ui.panes

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.chieppa.arc.core.pane.Page
import dev.chieppa.arc.ui.conversation.ConversationPage
import dev.chieppa.arc.ui.plugins.PluginPanePage
import dev.chieppa.arc.ui.pulls.FilterPage
import dev.chieppa.arc.ui.revisions.RevisionMatrixPage
import dev.chieppa.arc.ui.runs.RunPage
import dev.chieppa.arc.ui.runs.RunsPage
import dev.chieppa.arc.ui.settings.SettingsPage
import dev.chieppa.arc.ui.workbench.Workbench

/** What a page's tab shows, by its kind. */
@Composable
fun PageContent(page: Page, workbench: Workbench, modifier: Modifier = Modifier) {
    when (page) {
        is Page.Filter -> FilterPage(page, workbench, modifier)
        is Page.RevisionMatrix -> RevisionMatrixPage(page, workbench, modifier)
        is Page.Conversation -> ConversationPage(page, workbench, modifier)
        is Page.Runs -> RunsPage(page, workbench, modifier)
        is Page.Run -> RunPage(page, workbench, modifier)
        is Page.Settings -> SettingsPage(page, workbench, modifier)
        is Page.Plugin -> PluginPanePage(page, workbench, modifier)
    }
}

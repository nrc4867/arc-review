package dev.chieppa.arc.ui.mcp

import androidx.compose.runtime.Composable
import dev.chieppa.arc.core.pane.Page
import dev.chieppa.arc.design.ArcIcons
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.mcp_clients
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.frame.StatusItem
import org.jetbrains.compose.resources.pluralStringResource

/** The status bar's mark that MCP clients are connected to Arc, with how many, while any are; a click opens its settings. */
@Composable
fun McpStatusItem(workbench: Workbench) {
    val clients = workbench.mcp.clients
    if (clients == 0) return
    StatusItem(pluralStringResource(Res.plurals.mcp_clients, clients, clients), ArcIcons.Server) { openSection(workbench) }
}

private fun openSection(workbench: Workbench) {
    workbench.panes.openSettings()
    workbench.state.workspace.pages.filterIsInstance<Page.Settings>().firstOrNull()?.let { workbench.panes.showSettings(it, McpSettingsProvider.SECTION) }
}

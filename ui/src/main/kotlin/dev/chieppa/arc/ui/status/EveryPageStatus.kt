package dev.chieppa.arc.ui.status

import androidx.compose.runtime.Composable
import dev.chieppa.arc.ui.mcp.McpStatusItem
import dev.chieppa.arc.ui.update.UpdateStatusItem
import dev.chieppa.arc.ui.workbench.Workbench

/** The status bar items every page ends with: connected MCP clients and a newer release of Arc. */
@Composable
fun EveryPageStatus(workbench: Workbench) {
    UpdateStatusItem(workbench.updates)
    McpStatusItem(workbench)
}

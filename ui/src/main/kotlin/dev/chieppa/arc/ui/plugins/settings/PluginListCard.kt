package dev.chieppa.arc.ui.plugins.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import dev.chieppa.arc.plugins.host.PluginDiagnostic
import dev.chieppa.arc.plugins.host.PluginStatus
import dev.chieppa.arc.plugins.manager.PluginEntry
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.ui.Divider

/** The plugins card: every plugin found, with its toggle, capabilities and problems, in source order. */
@Composable
fun PluginListCard(workbench: Workbench) {
    val manager = workbench.plugins.manager ?: return
    val entries = workbench.plugins.entries
    Column {
        entries.forEachIndexed { index, entry ->
            if (index > 0) Divider()
            PluginEntryRow(entry, workbench.plugins.statuses[entry.id], problemsOf(entry, workbench.plugins.statuses[entry.id]), workbench) {
                manager.setEnabled(entry.id, it)
            }
        }
    }
}

/** What is wrong with [entry]: its manifest, or why its process failed. */
fun problemsOf(entry: PluginEntry, status: PluginStatus?): List<PluginDiagnostic> =
    listOfNotNull(entry.found.diagnostic) + ((status as? PluginStatus.Failed)?.diagnostics.orEmpty())

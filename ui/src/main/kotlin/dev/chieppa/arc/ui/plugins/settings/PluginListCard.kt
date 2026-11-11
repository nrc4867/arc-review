package dev.chieppa.arc.ui.plugins.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import dev.chieppa.arc.plugins.host.PluginDiagnostic
import dev.chieppa.arc.plugins.host.PluginStatus
import dev.chieppa.arc.plugins.manager.PluginEntry
import dev.chieppa.arc.plugins.typecheck.TypeErrors
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.ui.Divider

/** The plugins card: every plugin found, with its toggle, capabilities and problems, its first type errors among them, in source order. */
@Composable
fun PluginListCard(workbench: Workbench) {
    val manager = workbench.plugins.manager ?: return
    val entries = workbench.plugins.entries
    Column {
        entries.forEachIndexed { index, entry ->
            if (index > 0) Divider()
            val problems = problemsOf(entry, workbench.plugins.statuses[entry.id], workbench.plugins.typeErrors[entry.id])
            PluginEntryRow(entry, workbench.plugins.statuses[entry.id], problems, workbench) {
                manager.setEnabled(entry.id, it)
            }
        }
    }
}

/** What is wrong with [entry]: its manifest, why its process failed, and the first of its [types] errors. */
fun problemsOf(entry: PluginEntry, status: PluginStatus?, types: TypeErrors? = null): List<PluginDiagnostic> =
    listOfNotNull(entry.found.diagnostic) + ((status as? PluginStatus.Failed)?.diagnostics.orEmpty()) + types?.shown.orEmpty()

/** How many problems [entry] has, every type error counted. */
fun problemCount(entry: PluginEntry, status: PluginStatus?, types: TypeErrors?): Int =
    problemsOf(entry, status).size + (types?.total ?: 0)

package dev.chieppa.arc.ui.plugins

import androidx.compose.runtime.Composable
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.plugins_problems
import dev.chieppa.arc.design.generated.plugins_status
import dev.chieppa.arc.ui.plugins.settings.problemCount
import dev.chieppa.stretch.frame.StatusItem
import dev.chieppa.stretch.theme.StretchIcons
import org.jetbrains.compose.resources.pluralStringResource

/** The status bar's count of plugins on, of all found, while Settings shows the Plugins section: "4 of 5 plugins on". */
@Composable
fun PluginsStatusStart(plugins: PluginsController) {
    val entries = plugins.entries
    if (entries.isEmpty()) return
    val on = entries.count { it.on }
    StatusItem(pluginsStatusText(on, entries.size))
}

/** The status bar's count of the plugins' problems, every type error among them, with the error icon, while there are any. */
@Composable
fun PluginsStatusEnd(plugins: PluginsController) {
    val problems = plugins.entries.sumOf { problemCount(it, plugins.statuses[it.id], plugins.typeErrors[it.id]) }
    if (problems > 0) StatusItem(pluralStringResource(Res.plurals.plugins_problems, problems, problems), StretchIcons.Error)
}

@Composable
private fun pluginsStatusText(on: Int, all: Int): String = pluralStringResource(Res.plurals.plugins_status, all, on, all)

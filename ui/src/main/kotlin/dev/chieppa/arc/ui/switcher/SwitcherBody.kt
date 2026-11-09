package dev.chieppa.arc.ui.switcher

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.switcher_open_as
import dev.chieppa.arc.ui.pulls.currentTime
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.frame.PaletteSection
import dev.chieppa.stretch.theme.StretchSpacing
import org.jetbrains.compose.resources.stringResource

/**
 * The rows under a switcher field for [view]: the follow-a-repo row for a pasted folder, the card and ways to open a
 * pasted target, or the search's sections. The row at [index] is highlighted; a click picks a row through [commands].
 */
@Composable
fun SwitcherBody(workbench: Workbench, view: SwitcherView, query: String, index: Int, commands: SwitcherCommands) {
    val now = currentTime() / MILLIS_PER_SECOND
    val target = view.target
    val folder = view.folder
    when {
        folder != null -> Column(Modifier.padding(start = StretchSpacing.space6, end = StretchSpacing.space6, top = StretchSpacing.space4, bottom = StretchSpacing.space6)) {
            AddRepoRow(folder, view.folderFailed, highlighted = true) { view.addFolder(commands) }
        }
        target == null -> SearchResults(workbench, view.sections, query, index, now) { commands.pick(it, SwitcherPick.Switch) }
        else -> Column(Modifier.padding(start = StretchSpacing.space6, end = StretchSpacing.space6, bottom = StretchSpacing.space6)) {
            LinkCard(target, view.facts, now)
            if (view.options.isNotEmpty()) PaletteSection(stringResource(Res.string.switcher_open_as))
            view.options.forEachIndexed { i, option ->
                val held = option.source in workbench.state.workspace.sources
                OpenAsRow(option, view.facts, held, i == index) { commands.pick(option, SwitcherPick.Switch) }
            }
        }
    }
}

private const val MILLIS_PER_SECOND = 1000L

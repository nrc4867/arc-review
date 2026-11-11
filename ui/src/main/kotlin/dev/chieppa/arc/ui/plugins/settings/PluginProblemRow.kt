package dev.chieppa.arc.ui.plugins.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.plugins_open_file
import dev.chieppa.arc.plugins.host.PluginDiagnostic
import dev.chieppa.arc.plugins.manager.PluginEntry
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.theme.Icon
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchIcons
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.Button
import dev.chieppa.stretch.ui.ButtonKind
import dev.chieppa.stretch.ui.Label
import org.jetbrains.compose.resources.stringResource

/**
 * One problem with a plugin, highlighted in place: its file and line in the code face, its message, and a button that
 * opens the file where the system opens such files.
 */
@Composable
fun PluginProblemRow(entry: PluginEntry, problem: PluginDiagnostic, workbench: Workbench) {
    val c = Stretch.colors
    val shape = Stretch.shapes.ctlShape
    val place = problem.file?.let { file -> listOfNotNull(file, problem.line?.toString()).joinToString(PLACE_SEPARATOR) }
    Row(
        Modifier.fillMaxWidth().clip(shape).background(c.errorWash).border(Stretch.stroke, c.errorEdge, shape)
            .padding(horizontal = StretchSpacing.space10, vertical = StretchSpacing.space6).testTag(PluginSettingsTags.PROBLEM + entry.id),
        horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space8),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(StretchIcons.Error, c.errorInk)
        place?.let { Label(it, Stretch.type.valueSm, c.errorInk) }
        Label(problem.message, Stretch.type.bodySm, c.errorInk, Modifier.weight(1f))
        problem.file?.let { file ->
            Button(stringResource(Res.string.plugins_open_file), { workbench.links.open(entry.found.folder.resolve(file).toUri().toString()) }, kind = ButtonKind.Quiet, icon = StretchIcons.ExternalLink)
        }
    }
}

private const val PLACE_SEPARATOR = ":"

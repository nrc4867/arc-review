package dev.chieppa.arc.ui.plugins.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.plugins_enable
import dev.chieppa.arc.design.generated.plugins_origin
import dev.chieppa.arc.design.generated.plugins_restarting
import dev.chieppa.arc.design.generated.plugins_starting
import dev.chieppa.arc.plugins.config.PluginSourceSpec
import dev.chieppa.arc.plugins.host.PluginDiagnostic
import dev.chieppa.arc.plugins.host.PluginStatus
import dev.chieppa.arc.plugins.manager.PluginEntry
import dev.chieppa.arc.ui.plugins.PluginIconNames
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.theme.Icon
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.Label
import dev.chieppa.stretch.ui.Toggle
import org.jetbrains.compose.resources.stringResource
import java.nio.file.Path

/**
 * One plugin in the plugins card, as board 9 draws it: its icon, name, version and where it is from, its switch, its
 * capability chips beneath, and each problem highlighted in place with its file and line. A plugin with problems is
 * set apart by an accent edge.
 */
@Composable
fun PluginEntryRow(entry: PluginEntry, status: PluginStatus?, problems: List<PluginDiagnostic>, workbench: Workbench, onEnable: (Boolean) -> Unit) {
    val c = Stretch.colors
    val manifest = entry.found.manifest
    val title = manifest?.title ?: entry.id
    Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min).testTag(PluginSettingsTags.PLUGIN + entry.id)) {
        Box(Modifier.width(PluginSettingsSizes.problemEdge).fillMaxHeight().background(if (problems.isNotEmpty()) c.errorEdge else c.surfacePanel.copy(alpha = 0f)))
        Column(
            Modifier.weight(1f).padding(horizontal = StretchSpacing.space14, vertical = StretchSpacing.space12),
            verticalArrangement = Arrangement.spacedBy(StretchSpacing.space8),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space10), verticalAlignment = Alignment.CenterVertically) {
                Icon(PluginIconNames.orPlugin(manifest?.icon), c.inkControl)
                Label(title, Stretch.type.body.copy(fontWeight = FontWeight.SemiBold), c.inkStrong)
                Label(stringResource(Res.string.plugins_origin, manifest?.version.orEmpty(), originOf(entry)), Stretch.type.bodySm, c.inkSubtle, Modifier.weight(1f))
                statusText(status)?.let { Label(it, Stretch.type.bodySm, c.inkSubtle) }
                Toggle(entry.enabled, onEnable, stringResource(Res.string.plugins_enable, title))
            }
            Column(Modifier.padding(start = PluginSettingsSizes.detailIndent), verticalArrangement = Arrangement.spacedBy(StretchSpacing.space6)) {
                manifest?.let { PluginCapabilityChips(it.capabilities) }
                problems.forEach { PluginProblemRow(entry, it, workbench) }
            }
        }
    }
}

@Composable
private fun statusText(status: PluginStatus?): String? = when (status) {
    PluginStatus.Starting -> stringResource(Res.string.plugins_starting)
    is PluginStatus.Restarting -> stringResource(Res.string.plugins_restarting)
    else -> null
}

/** Where a plugin is from: its folder's parent for a directory source, else the source's url. */
private fun originOf(entry: PluginEntry): String = when (val source = entry.found.source) {
    is PluginSourceSpec.Directory -> runCatching { Path.of(source.path).fileName?.toString() }.getOrNull() ?: source.path
    is PluginSourceSpec.Git -> source.url.substringAfterLast('/')
}

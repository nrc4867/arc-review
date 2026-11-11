package dev.chieppa.arc.ui.plugins.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.plugins_add_source
import dev.chieppa.arc.design.generated.plugins_remove_source
import dev.chieppa.arc.design.generated.plugins_source_count
import org.jetbrains.compose.resources.pluralStringResource
import dev.chieppa.arc.design.generated.plugins_source_directory
import dev.chieppa.arc.design.generated.plugins_source_git
import dev.chieppa.arc.design.generated.plugins_source_placeholder
import dev.chieppa.arc.plugins.config.PluginSourceSpec
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.theme.Icon
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchIcons
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.Button
import dev.chieppa.stretch.ui.ButtonKind
import dev.chieppa.stretch.ui.Divider
import dev.chieppa.stretch.ui.IconButton
import dev.chieppa.stretch.ui.Label
import dev.chieppa.stretch.ui.TextField
import org.jetbrains.compose.resources.stringResource
import java.nio.file.InvalidPathException
import java.nio.file.Path

/**
 * The plugin sources card: each folder or git repo plugins come from, with how many plugins it holds and a button to
 * remove it, and a field to add a folder by its path.
 */
@Composable
fun PluginSourcesCard(workbench: Workbench) {
    val manager = workbench.plugins.manager ?: return
    val c = Stretch.colors
    val sources = workbench.plugins.config.sources
    Column {
        for (source in sources) {
            val count = workbench.plugins.entries.count { it.found.source == source }
            Row(
                Modifier.fillMaxWidth().heightIn(min = PluginSettingsSizes.sourceRow).padding(horizontal = StretchSpacing.space14),
                horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space12),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(StretchIcons.Folder, c.inkControl)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(StretchSpacing.space2)) {
                    Label(nameOf(source), Stretch.type.valueSm.copy(fontWeight = FontWeight.SemiBold), c.inkStrong)
                    val kind = when (source) {
                        is PluginSourceSpec.Directory -> stringResource(Res.string.plugins_source_directory)
                        is PluginSourceSpec.Git -> stringResource(Res.string.plugins_source_git, source.ref)
                    }
                    Label(pluralStringResource(Res.plurals.plugins_source_count, count, kind, count), Stretch.type.bodySm, c.inkSubtle)
                }
                IconButton(StretchIcons.Close, stringResource(Res.string.plugins_remove_source), { manager.removeSource(source) })
            }
            Divider()
        }
        AddSourceRow { manager.addDirectory(it) }
    }
}

@Composable
private fun AddSourceRow(onAdd: (Path) -> Unit) {
    var path by rememberSaveable { mutableStateOf("") }
    val parsed = path.trim().takeIf { it.isNotEmpty() }?.let { text -> try { Path.of(text) } catch (_: InvalidPathException) { null } }
    Row(
        Modifier.fillMaxWidth().padding(horizontal = StretchSpacing.space14, vertical = StretchSpacing.space10),
        horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space8),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextField(path, { path = it }, Modifier.weight(1f).testTag(PluginSettingsTags.ADD_SOURCE), placeholder = stringResource(Res.string.plugins_source_placeholder), icon = StretchIcons.Folder, mono = true)
        Button(stringResource(Res.string.plugins_add_source), { parsed?.let(onAdd); path = "" }, kind = ButtonKind.Quiet, icon = StretchIcons.Plus, enabled = parsed != null)
    }
}

private fun nameOf(source: PluginSourceSpec): String = when (source) {
    is PluginSourceSpec.Directory -> source.path
    is PluginSourceSpec.Git -> source.url
}

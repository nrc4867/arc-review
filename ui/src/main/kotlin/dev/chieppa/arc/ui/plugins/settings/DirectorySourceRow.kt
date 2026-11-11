package dev.chieppa.arc.ui.plugins.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.plugins_remove_source
import dev.chieppa.arc.design.generated.plugins_source_count
import dev.chieppa.arc.design.generated.plugins_source_directory
import dev.chieppa.arc.plugins.config.PluginSourceSpec
import dev.chieppa.stretch.theme.Icon
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchIcons
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.IconButton
import dev.chieppa.stretch.ui.Label
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/** A folder source's row: its path, that it is watched, how many plugins it holds, and a button to remove it. */
@Composable
fun DirectorySourceRow(source: PluginSourceSpec.Directory, plugins: Int, onRemove: () -> Unit) {
    val c = Stretch.colors
    Row(
        Modifier.fillMaxWidth().heightIn(min = PluginSettingsSizes.sourceRow).padding(horizontal = StretchSpacing.space14),
        horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(StretchIcons.Folder, c.inkControl)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(StretchSpacing.space2)) {
            Label(source.path, Stretch.type.valueSm.copy(fontWeight = FontWeight.SemiBold), c.inkStrong)
            Label(pluralStringResource(Res.plurals.plugins_source_count, plugins, stringResource(Res.string.plugins_source_directory), plugins), Stretch.type.bodySm, c.inkSubtle)
        }
        IconButton(StretchIcons.Close, stringResource(Res.string.plugins_remove_source), onRemove)
    }
}

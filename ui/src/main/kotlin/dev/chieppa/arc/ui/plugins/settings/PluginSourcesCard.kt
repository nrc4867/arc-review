package dev.chieppa.arc.ui.plugins.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.plugins_add_source
import dev.chieppa.arc.design.generated.plugins_source_not_full
import dev.chieppa.arc.design.generated.plugins_source_placeholder
import dev.chieppa.arc.plugins.config.PluginSourceSpec
import dev.chieppa.arc.plugins.gitsource.GitSourceInput
import dev.chieppa.arc.ui.plugins.GitUpdateReview
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.arc.ui.util.FolderPathField
import dev.chieppa.arc.ui.util.TypedPath
import dev.chieppa.stretch.theme.StretchIcons
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.Button
import dev.chieppa.stretch.ui.ButtonKind
import dev.chieppa.stretch.ui.Divider
import org.jetbrains.compose.resources.stringResource
import java.nio.file.Path

/**
 * The plugin sources card: each folder or git repo plugins come from, with how many plugins it holds and a button to
 * remove it, a git source's pin and its waiting update, a field to add a folder by its path, typed as [TypedPath] reads
 * it or chosen with the field's folder button, or a git repo by its url and ref, and a row making a new plugin from
 * Arc's starter.
 */
@Composable
fun PluginSourcesCard(workbench: Workbench) {
    val manager = workbench.plugins.manager ?: return
    val sources = workbench.plugins.config.sources
    Column {
        for (source in sources) {
            val count = workbench.plugins.entries.count { it.found.source == source }
            when (source) {
                is PluginSourceSpec.Directory -> DirectorySourceRow(source, count) { manager.removeSource(source) }
                is PluginSourceSpec.Git -> GitSourceRow(
                    source,
                    workbench.plugins.gitStates[source.url],
                    count,
                    workbench.plugins.now(),
                    onReview = { GitUpdateReview.open(workbench, source, it) },
                    onAccept = { manager.acceptUpdate(source.url) },
                    onRemove = { manager.removeSource(source) },
                )
            }
            Divider()
        }
        AddSourceRow({ manager.addDirectory(it) }, { url, ref -> manager.addGit(url, ref) })
        Divider()
        NewPluginRow(workbench.dispatchers.io) { manager.addDirectory(it) }
    }
}

@Composable
private fun AddSourceRow(onFolder: (Path) -> Unit, onGit: (String, String) -> Unit) {
    var text by rememberSaveable { mutableStateOf("") }
    val git = GitSourceInput.parse(text)
    val folder = if (git == null) TypedPath.read(text) else null
    Row(
        Modifier.fillMaxWidth().padding(horizontal = StretchSpacing.space14, vertical = StretchSpacing.space10),
        horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space8),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FolderPathField(
            text,
            { text = it },
            stringResource(Res.string.plugins_source_placeholder),
            Modifier.weight(1f).testTag(PluginSettingsTags.ADD_SOURCE),
            error = stringResource(Res.string.plugins_source_not_full).takeIf { text.isNotBlank() && git == null && folder == null },
        )
        Button(
            stringResource(Res.string.plugins_add_source),
            {
                git?.let { (url, ref) -> onGit(url, ref) } ?: folder?.let(onFolder)
                text = ""
            },
            kind = ButtonKind.Quiet,
            icon = StretchIcons.Plus,
            enabled = git != null || folder != null,
        )
    }
}

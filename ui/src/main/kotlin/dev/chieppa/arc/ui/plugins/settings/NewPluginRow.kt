package dev.chieppa.arc.ui.plugins.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.plugins_new
import dev.chieppa.arc.design.generated.plugins_new_bad_name
import dev.chieppa.arc.design.generated.plugins_new_folder_taken
import dev.chieppa.arc.design.generated.plugins_new_name
import dev.chieppa.arc.design.generated.plugins_new_parent
import dev.chieppa.arc.plugins.starter.PluginStarter
import dev.chieppa.arc.plugins.starter.StarterException
import dev.chieppa.arc.plugins.starter.StarterName
import dev.chieppa.arc.plugins.starter.StarterProblem
import dev.chieppa.stretch.theme.StretchIcons
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.Button
import dev.chieppa.stretch.ui.ButtonKind
import dev.chieppa.stretch.ui.TextField
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.stringResource
import java.nio.file.InvalidPathException
import java.nio.file.Path

/**
 * Makes a new plugin from Arc's starter: the folder to make it in and its name, typed, and a button that writes it on
 * [io] and gives its folder to [onCreated], which adds it as a source. A name that makes no id, and a folder holding
 * files already, are highlighted on their fields.
 */
@Composable
fun NewPluginRow(io: CoroutineDispatcher, onCreated: (Path) -> Unit) {
    var parent by rememberSaveable { mutableStateOf("") }
    var name by rememberSaveable { mutableStateOf("") }
    var failure by rememberSaveable { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val folder = parent.trim().takeIf { it.isNotEmpty() }?.let { typed -> try { Path.of(typed) } catch (_: InvalidPathException) { null } }
    val named = StarterName.of(name)
    Row(
        Modifier.fillMaxWidth().padding(horizontal = StretchSpacing.space14, vertical = StretchSpacing.space10),
        horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space8),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextField(
            parent,
            { parent = it; failure = null },
            Modifier.weight(2f).testTag(PluginSettingsTags.NEW_PARENT),
            placeholder = stringResource(Res.string.plugins_new_parent),
            icon = StretchIcons.Folder,
            error = failure,
            mono = true,
        )
        TextField(
            name,
            { name = it; failure = null },
            Modifier.weight(1f).testTag(PluginSettingsTags.NEW_NAME),
            placeholder = stringResource(Res.string.plugins_new_name),
            error = stringResource(Res.string.plugins_new_bad_name).takeIf { name.isNotBlank() && named == null },
        )
        val taken = stringResource(Res.string.plugins_new_folder_taken)
        Button(
            stringResource(Res.string.plugins_new),
            {
                val into = folder ?: return@Button
                scope.launch {
                    val made = withContext(io) { runCatching { PluginStarter.create(into, name) } }
                    made.onSuccess { onCreated(it); name = "" }
                    failure = (made.exceptionOrNull() as? StarterException)?.let { if (it.problem == StarterProblem.FolderTaken) "$taken: ${it.detail}" else it.detail }
                }
            },
            Modifier.testTag(PluginSettingsTags.NEW_CREATE),
            kind = ButtonKind.Quiet,
            icon = StretchIcons.Plus,
            enabled = folder != null && named != null,
        )
    }
}

package dev.chieppa.arc.ui.sources

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.chieppa.arc.core.source.RepoName
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.add_source_add
import dev.chieppa.arc.design.generated.add_source_add_repo
import dev.chieppa.arc.design.generated.add_source_folder
import dev.chieppa.arc.design.generated.add_source_ref_hint
import dev.chieppa.arc.design.generated.add_source_repo
import dev.chieppa.arc.design.generated.add_source_title
import dev.chieppa.arc.ui.util.ArcSizes
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.frame.Dropdown
import dev.chieppa.stretch.frame.Popover
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.Button
import dev.chieppa.stretch.ui.ButtonKind
import dev.chieppa.stretch.ui.Divider
import dev.chieppa.stretch.ui.TextField
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import java.nio.file.InvalidPathException
import java.nio.file.Path

/**
 * What the strip's add button opens: pick a repo and type a source in it (`@sha`, `@sha..sha` or `:branch...base`),
 * or add a repo by its folder. A field whose text names nothing is edged in the error color.
 */
@Composable
fun AddSourcePopover(workbench: Workbench) {
    val repos = workbench.state.repos.keys.sortedBy { it.value }
    var repo by remember { mutableStateOf<RepoName?>(null) }
    var ref by remember { mutableStateOf("") }
    var folder by remember { mutableStateOf("") }
    var refFailed by remember { mutableStateOf(false) }
    var folderFailed by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val chosen = repo?.takeIf { it in repos } ?: repos.firstOrNull()

    fun addSource() {
        val name = chosen ?: return
        val text = ref.trim()
        val full = if (text.startsWith(name.value)) text else name.value + text
        refFailed = !workbench.addSource(full)
        if (!refFailed) {
            ref = ""
            workbench.addSourceOpen = false
        }
    }

    fun addRepo() {
        val dir = try {
            Path.of(folder.trim())
        } catch (_: InvalidPathException) {
            null
        }
        scope.launch {
            val added = dir?.let { workbench.sources.addRepo(it) }
            folderFailed = added == null
            if (added != null) {
                repo = added
                folder = ""
            }
        }
    }

    Popover(workbench.addSourceOpen, { workbench.addSourceOpen = false }, title = stringResource(Res.string.add_source_title), width = ArcSizes.popoverWidth) {
        val rowModifier = Modifier.fillMaxWidth().padding(vertical = StretchSpacing.space4)
        if (chosen != null) {
            Dropdown(repos, chosen, { repo = it }, { it.value }, stringResource(Res.string.add_source_repo), rowModifier)
            Row(rowModifier, horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space8), verticalAlignment = Alignment.CenterVertically) {
                TextField(
                    ref,
                    { ref = it; refFailed = false },
                    Modifier.weight(1f),
                    placeholder = stringResource(Res.string.add_source_ref_hint),
                    error = if (refFailed) "" else null,
                )
                Button(stringResource(Res.string.add_source_add), ::addSource, kind = ButtonKind.Primary)
            }
            Divider(Modifier.padding(vertical = StretchSpacing.space4))
        }
        Row(rowModifier, horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space8), verticalAlignment = Alignment.CenterVertically) {
            TextField(
                folder,
                { folder = it; folderFailed = false },
                Modifier.weight(1f),
                placeholder = stringResource(Res.string.add_source_folder),
                error = if (folderFailed) "" else null,
            )
            Button(stringResource(Res.string.add_source_add_repo), ::addRepo)
        }
    }
}

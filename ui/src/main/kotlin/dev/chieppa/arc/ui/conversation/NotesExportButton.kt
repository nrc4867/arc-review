package dev.chieppa.arc.ui.conversation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import dev.chieppa.arc.core.conversation.NotesExport
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.notes_export
import dev.chieppa.arc.design.generated.notes_file_kind
import dev.chieppa.arc.design.generated.notes_on_lines
import dev.chieppa.arc.design.generated.notes_title
import dev.chieppa.arc.ui.util.SourceLabels
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.document.rememberFileChooser
import dev.chieppa.stretch.theme.StretchIcons
import dev.chieppa.arc.ui.util.TipIconButton
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.stringResource
import java.io.File

/** The Notes drawer's export button: the active source's notes saved as one Markdown file where the person picks. */
@Composable
fun NotesExportButton(workbench: Workbench) {
    val source = workbench.state.active ?: return
    val chooser = rememberFileChooser()
    val scope = rememberCoroutineScope()
    val format = NotesFormat(stringResource(Res.string.notes_file_kind))
    val title = stringResource(Res.string.notes_title, SourceLabels.of(source))
    val onLines = stringResource(Res.string.notes_on_lines)
    TipIconButton(StretchIcons.Export, stringResource(Res.string.notes_export), {
        val text = NotesExport.markdown(workbench.comments.controller.local, source, title, onLines)
        scope.launch {
            val target = chooser.chooseSave(format, NotesExport.fileName(source)) ?: return@launch
            withContext(workbench.dispatchers.io) { File(target).writeBytes(format.encode(text)) }
        }
    })
}

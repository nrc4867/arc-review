package dev.chieppa.arc.ui.conversation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import dev.chieppa.arc.design.ArcIcons
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.notes_count
import dev.chieppa.arc.design.generated.notes_kept_here
import dev.chieppa.arc.design.generated.notes_on_lines
import dev.chieppa.arc.design.generated.notes_on_source
import dev.chieppa.arc.design.generated.staged_location
import dev.chieppa.arc.design.generated.staged_separator
import dev.chieppa.arc.ui.util.ArcSizes
import dev.chieppa.arc.ui.util.SourceLabels
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.markdown.MarkdownEditor
import dev.chieppa.stretch.markdown.rememberMarkdownEditorState
import dev.chieppa.stretch.theme.Icon
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchSizes
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.Divider
import kotlinx.coroutines.flow.drop
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/**
 * The Notes drawer for the active source: the person's Markdown note on the source as a whole, written in place, and
 * their notes on lines, each opening its file. Notes stay on this machine. Plain while there is no source.
 */
@Composable
fun NotesDrawer(workbench: Workbench) {
    val source = workbench.state.active ?: return
    val controller = workbench.comments.controller
    val notes = controller.local.of(source).filter { it.isNote }
    val onLines = notes.filter { it.anchor != null }.sortedWith(compareBy({ it.anchor?.path }, { it.anchor?.line }))
    val c = Stretch.colors
    val muted = Stretch.type.bodySm.merge(TextStyle(color = c.inkMuted))
    Column(Modifier.fillMaxSize()) {
        SourceHeading(workbench.state, source, Modifier.padding(StretchSpacing.space12)) {
            val parts = listOf(stringResource(Res.string.notes_kept_here), pluralStringResource(Res.plurals.notes_count, notes.size, notes.size))
            BasicText(parts.joinToString(stringResource(Res.string.staged_separator)), style = muted, maxLines = 1)
        }
        Divider()
        BasicText(stringResource(Res.string.notes_on_source, SourceLabels.of(source)), Modifier.padding(StretchSpacing.space12), style = Stretch.type.eyebrow)
        key(source) {
            val editor = rememberMarkdownEditorState(controller.sourceNote(source)?.body.orEmpty())
            LaunchedEffect(editor) { snapshotFlow { editor.text }.drop(1).collect { controller.writeSourceNote(source, it) } }
            MarkdownEditor(editor, Modifier.weight(1f).fillMaxWidth().padding(horizontal = StretchSpacing.space12), showToolbar = false)
        }
        if (onLines.isEmpty()) return@Column
        Divider()
        BasicText(stringResource(Res.string.notes_on_lines), Modifier.padding(StretchSpacing.space12), style = Stretch.type.eyebrow)
        Column(Modifier.fillMaxWidth().heightIn(max = ArcSizes.notesOnLines).verticalScroll(rememberScrollState()).padding(bottom = StretchSpacing.space8)) {
            for (note in onLines) {
                val anchor = note.anchor ?: continue
                Row(
                    Modifier.fillMaxWidth().heightIn(min = StretchSizes.row).clickable { workbench.panes.openChange(source, anchor.path) }.padding(horizontal = StretchSpacing.space12),
                    horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space8),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(ArcIcons.Note, c.inkMuted, size = ArcSizes.pullIcon)
                    BasicText(stringResource(Res.string.staged_location, anchor.path.substringAfterLast('/'), anchor.line), style = Stretch.type.valueSm.merge(TextStyle(color = c.inkStrong)), maxLines = 1)
                    BasicText(note.body.lineSequence().firstOrNull().orEmpty(), Modifier.weight(1f), style = muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

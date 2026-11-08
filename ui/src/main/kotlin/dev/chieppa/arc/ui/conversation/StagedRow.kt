package dev.chieppa.arc.ui.conversation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import dev.chieppa.arc.core.conversation.StagedComment
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.comment_delete
import dev.chieppa.arc.design.generated.staged_edit
import dev.chieppa.arc.design.generated.staged_from_pending
import dev.chieppa.arc.design.generated.staged_line_gone
import dev.chieppa.arc.design.generated.staged_location
import dev.chieppa.arc.design.generated.staged_menu
import dev.chieppa.arc.design.generated.staged_moved_from
import dev.chieppa.arc.design.generated.staged_open
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.frame.Menu
import dev.chieppa.stretch.frame.MenuItem
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchIcons
import dev.chieppa.stretch.theme.StretchSizes
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.IconButton
import dev.chieppa.stretch.ui.PathLabel
import org.jetbrains.compose.resources.stringResource

/**
 * One staged comment: how it will go up, where it sits now and what it says, with a menu to open its line, take it
 * back to writing, or delete it. One whose line is gone, or that the last push could not send, is washed in the
 * warning colors in place; one moved with its lines says where it was written.
 */
@Composable
fun StagedRow(comment: StagedComment, workbench: Workbench, modifier: Modifier = Modifier) {
    val c = Stretch.colors
    val controller = workbench.comments.controller
    val gone = comment.lineGoneIn
    val warn = gone != null || comment.failed
    val open = { comment.path?.let { workbench.panes.openChange(comment.source, it, newTab = false) }; Unit }
    Column(
        modifier.fillMaxWidth().background(if (warn) c.warnWash else Color.Transparent).clickable(onClick = open)
            .padding(horizontal = StretchSpacing.space12, vertical = StretchSpacing.space8),
        verticalArrangement = Arrangement.spacedBy(StretchSpacing.space4),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space8), verticalAlignment = Alignment.CenterVertically) {
            val (text, icon) = routeChip(comment.route)
            StateChip(text, icon, stagedLook(warn))
            val mono = Stretch.type.valueSm
            val location = comment.path?.let { path -> comment.line?.let { stringResource(Res.string.staged_location, path, it) } ?: path }
            if (location != null) {
                val style = if (gone != null) mono.merge(TextStyle(textDecoration = TextDecoration.LineThrough)) else mono
                PathLabel(location, style, if (warn) c.warnInk else c.inkStrong, Modifier.weight(1f, fill = false))
            }
            val movedFrom = comment.movedFrom
            when {
                gone != null -> StateChip(stringResource(Res.string.staged_line_gone, gone), StretchIcons.Warn, stagedLook(true))
                movedFrom != null -> BasicText(stringResource(Res.string.staged_moved_from, movedFrom), style = Stretch.type.chip.merge(TextStyle(color = c.inkMuted)))
                comment.fromPending -> BasicText(stringResource(Res.string.staged_from_pending), style = Stretch.type.chip.merge(TextStyle(color = c.inkMuted)))
            }
            Spacer(Modifier.weight(1f))
            RowMenu(comment, workbench, open, { controller.edit(comment.id) }, { controller.discard(comment.id) })
        }
        BasicText(comment.body, Modifier.padding(start = StretchSpacing.space2), style = Stretch.type.bodySm.merge(TextStyle(color = c.ink)))
    }
}

@Composable
private fun RowMenu(comment: StagedComment, workbench: Workbench, open: () -> Unit, edit: () -> Unit, delete: () -> Unit) {
    var shown by remember { mutableStateOf(false) }
    Box {
        IconButton(StretchIcons.More, stringResource(Res.string.staged_menu), { shown = true }, size = StretchSizes.seg)
        Menu(shown, { shown = false }) {
            if (comment.path != null) MenuItem(stringResource(Res.string.staged_open), { shown = false; open() })
            if (!comment.fromPending) {
                MenuItem(stringResource(Res.string.staged_edit), { shown = false; edit(); open() }, icon = StretchIcons.Pen)
                MenuItem(stringResource(Res.string.comment_delete), { shown = false; delete() })
            }
        }
    }
}

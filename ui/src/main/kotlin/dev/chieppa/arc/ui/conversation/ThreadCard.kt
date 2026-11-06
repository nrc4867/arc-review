package dev.chieppa.arc.ui.conversation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import dev.chieppa.arc.core.conversation.ReviewThread
import dev.chieppa.arc.core.conversation.ThreadState
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.thread_adopt
import dev.chieppa.arc.design.generated.thread_dismiss
import dev.chieppa.arc.design.generated.thread_fold
import dev.chieppa.arc.design.generated.thread_lines_changed
import dev.chieppa.arc.design.generated.thread_line
import dev.chieppa.arc.design.generated.thread_replies
import dev.chieppa.arc.design.generated.thread_reply
import dev.chieppa.arc.design.generated.thread_resolve
import dev.chieppa.arc.ui.util.ArcSizes
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchIcons
import dev.chieppa.stretch.theme.StretchSizes
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.Button
import dev.chieppa.stretch.ui.ButtonKind
import dev.chieppa.stretch.ui.Divider
import dev.chieppa.stretch.ui.IconButton
import dev.chieppa.stretch.ui.framedSurface
import org.jetbrains.compose.resources.stringResource

/**
 * A thread under its line: a head with its state, line and where it stands, and when [expanded] its messages and the
 * actions for its state. A guest note is drawn in the guest colors and offers [onAdopt] and [onDismiss].
 */
@Composable
fun ThreadCard(
    thread: ReviewThread,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    onAdopt: () -> Unit = {},
    onDismiss: () -> Unit = {},
) {
    val c = Stretch.colors
    val look = stateLook(thread.state)
    val edge = if (thread.state == ThreadState.Open || thread.state == ThreadState.Draft) c.lineStrong else look.edge
    val fill = if (thread.state == ThreadState.Guest) look.fill else c.surfaceFloat
    Column(
        modifier
            .padding(horizontal = StretchSpacing.space24, vertical = StretchSpacing.space6)
            .widthIn(max = ArcSizes.threadMaxWidth)
            .fillMaxWidth()
            .framedSurface(Stretch.shapes.cardShape, fill, edge, Stretch.stroke),
    ) {
        Row(
            Modifier.fillMaxWidth().clickable(onClick = onToggle).padding(horizontal = StretchSpacing.space10, vertical = StretchSpacing.space6),
            horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space8),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val (text, icon) = threadChip(thread.state)
            StateChip(text, icon, look)
            val muted = Stretch.type.valueSm.merge(TextStyle(color = c.inkMuted))
            val first = thread.messages.firstOrNull()
            val line = stringResource(Res.string.thread_line, thread.line)
            val summary = if (expanded || first == null) "$line · ${thread.source.ref}" else "$line  ${first.author}: ${first.body}"
            BasicText(summary, Modifier.weight(1f), style = muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            thread.changedIn?.let { BasicText(stringResource(Res.string.thread_lines_changed, it), style = muted.merge(TextStyle(color = look.ink))) }
            val replies = thread.messages.size - 1
            if (!expanded && replies > 0) BasicText(stringResource(Res.string.thread_replies, replies), style = muted)
            IconButton(if (expanded) StretchIcons.ChevronDown else StretchIcons.ChevronRight, stringResource(Res.string.thread_fold), onToggle, size = StretchSizes.seg)
        }
        if (!expanded) return@Column
        for (message in thread.messages) {
            Divider()
            ThreadMessageRow(message)
        }
        Divider()
        Row(
            Modifier.fillMaxWidth().background(if (thread.state == ThreadState.Guest) look.fill else c.surfaceFloat).padding(StretchSpacing.space6),
            horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space6),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            when (thread.state) {
                ThreadState.Guest -> {
                    Button(stringResource(Res.string.thread_adopt), onAdopt, kind = ButtonKind.Quiet, icon = StretchIcons.Pen)
                    Button(stringResource(Res.string.thread_dismiss), onDismiss, kind = ButtonKind.Quiet, icon = StretchIcons.Close)
                }
                ThreadState.Draft -> Unit
                else -> {
                    Button(stringResource(Res.string.thread_reply), {}, kind = ButtonKind.Quiet, icon = dev.chieppa.arc.design.ArcIcons.Comment)
                    if (thread.state != ThreadState.Resolved) Button(stringResource(Res.string.thread_resolve), {}, kind = ButtonKind.Quiet, icon = StretchIcons.Check)
                }
            }
            BasicText(thread.link.orEmpty(), Modifier.weight(1f).padding(end = StretchSpacing.space6), style = Stretch.type.bodySm.merge(TextStyle(color = c.inkMuted, textAlign = androidx.compose.ui.text.style.TextAlign.End)), maxLines = 1)
        }
    }
}

package dev.chieppa.arc.ui.conversation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import dev.chieppa.arc.core.conversation.Reaction
import dev.chieppa.arc.core.conversation.ReviewThread
import dev.chieppa.arc.core.conversation.ThreadState
import dev.chieppa.arc.design.ArcIcons
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.staged_location
import dev.chieppa.arc.design.generated.thread_fold
import dev.chieppa.arc.design.generated.thread_go_to
import dev.chieppa.arc.design.generated.thread_line
import dev.chieppa.arc.design.generated.thread_reply
import dev.chieppa.arc.design.generated.thread_replies
import dev.chieppa.arc.ui.util.TipIconButton
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchIcons
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.Button
import dev.chieppa.stretch.ui.ButtonKind
import dev.chieppa.stretch.ui.Divider
import dev.chieppa.stretch.ui.PathLabel
import dev.chieppa.stretch.ui.Tooltip
import dev.chieppa.stretch.ui.framedSurface
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/**
 * A thread as the conversation shows it: its state and where it is, file and line, which goes to the line with the
 * thread open, then its messages and a reply or resolve. It folds as it does under its line, so a resolved thread
 * starts folded. [showPath] leaves the file out when a heading names it already.
 */
@Composable
fun ConversationThread(thread: ReviewThread, workbench: Workbench, modifier: Modifier = Modifier, showPath: Boolean = true) {
    val c = Stretch.colors
    val controller = workbench.comments.controller
    val views = workbench.threadViews
    val expanded = views.expanded(thread)
    val look = stateLook(thread.state)
    val edge = if (thread.state == ThreadState.Open) c.lineStrong else look.edge
    Column(modifier.fillMaxWidth().framedSurface(Stretch.shapes.cardShape, c.surfaceFloat, edge, Stretch.stroke)) {
        Row(
            Modifier.fillMaxWidth().clickable { views.toggle(thread) }.padding(horizontal = StretchSpacing.space10, vertical = StretchSpacing.space6),
            horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space8),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val (text, icon) = threadChip(thread.state)
            StateChip(text, icon, look)
            val where = if (showPath) stringResource(Res.string.staged_location, thread.path, thread.line) else stringResource(Res.string.thread_line, thread.line)
            Tooltip(stringResource(Res.string.thread_go_to)) {
                PathLabel(where, Stretch.type.valueSm, c.accent, Modifier.clickable { ThreadJumps.to(workbench, thread) })
            }
            Spacer(Modifier.weight(1f))
            val replies = thread.messages.size - 1
            if (!expanded && replies > 0) {
                BasicText(pluralStringResource(Res.plurals.thread_replies, replies, replies), style = Stretch.type.valueSm.merge(TextStyle(color = c.inkMuted)))
            }
            TipIconButton(if (expanded) StretchIcons.ChevronDown else StretchIcons.ChevronRight, stringResource(Res.string.thread_fold), { views.toggle(thread) })
        }
        if (!expanded) return@Column
        for (message in thread.messages) {
            Divider()
            ThreadMessageRow(message, workbench, onReact = { r: Reaction -> controller.react(thread.source, message.id, r) }.takeIf { thread.hosted })
        }
        if (!thread.hosted) return@Column
        Divider()
        CardFooter(c.surfaceFloat) {
            Button(
                stringResource(Res.string.thread_reply),
                { if (ThreadJumps.to(workbench, thread)) controller.startReply(thread) },
                kind = ButtonKind.Quiet,
                icon = ArcIcons.Comment,
            )
            ResolveButton(thread, controller)
        }
    }
}

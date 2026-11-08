package dev.chieppa.arc.ui.conversation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import dev.chieppa.arc.core.conversation.LocalKind
import dev.chieppa.arc.core.conversation.Reaction
import dev.chieppa.arc.core.conversation.ReviewThread
import dev.chieppa.arc.core.conversation.ThreadState
import dev.chieppa.arc.design.ArcIcons
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.staged_replies
import dev.chieppa.arc.design.generated.thread_adopt
import dev.chieppa.arc.design.generated.thread_dismiss
import dev.chieppa.arc.design.generated.thread_lines_changed
import dev.chieppa.arc.design.generated.thread_reply
import dev.chieppa.arc.design.generated.thread_replies
import dev.chieppa.arc.ui.util.ArcSizes
import dev.chieppa.arc.ui.util.SourceLabels
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchIcons
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.Button
import dev.chieppa.stretch.ui.ButtonKind
import dev.chieppa.stretch.ui.Divider
import dev.chieppa.stretch.ui.framedSurface
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/**
 * A thread from the host or a guest under its line: a head with its state, line and where it stands, and when open
 * its messages with their reactions, the person's replies, and the actions for its state. A host's thread takes a
 * reply, which stages with the review, and resolving, which goes up at once. A guest note is drawn in the guest colors
 * and is adopted or dismissed.
 */
@Composable
fun ThreadCard(thread: ReviewThread, workbench: Workbench, modifier: Modifier = Modifier) {
    val c = Stretch.colors
    val controller = workbench.comments.controller
    val views = workbench.threadViews
    val expanded = views.expanded(thread)
    val look = stateLook(thread.state)
    val guest = thread.state == ThreadState.Guest
    val edge = if (thread.state == ThreadState.Open) c.lineStrong else look.edge
    val fill = if (guest) look.fill else c.surfaceFloat
    val writing = thread.replies.firstOrNull { it.kind == LocalKind.Draft && it.id in controller.writing }
    val staged = thread.replies.count { it.kind == LocalKind.Staged }
    Column(
        modifier
            .padding(horizontal = StretchSpacing.space24, vertical = StretchSpacing.space6)
            .widthIn(max = ArcSizes.threadMaxWidth)
            .fillMaxWidth()
            .framedSurface(Stretch.shapes.cardShape, fill, edge, Stretch.stroke),
    ) {
        val first = thread.messages.firstOrNull()
        ThreadHead(thread, expanded, { views.toggle(thread) }, SourceLabels.of(thread.source), first?.let { "${it.author}: ${it.body}" }) {
            val muted = Stretch.type.valueSm.merge(TextStyle(color = c.inkMuted))
            thread.changedIn?.let { BasicText(stringResource(Res.string.thread_lines_changed, it), style = muted.merge(TextStyle(color = look.ink))) }
            if (staged > 0) StateChip(pluralStringResource(Res.plurals.staged_replies, staged, staged), ArcIcons.Stage, stagedLook(false))
            val replies = thread.messages.size - 1
            if (!expanded && replies > 0) BasicText(pluralStringResource(Res.plurals.thread_replies, replies, replies), style = muted)
        }
        if (!expanded) return@Column
        for (message in thread.messages) {
            Divider()
            ThreadMessageRow(message, workbench, onReact = { r: Reaction -> controller.react(thread.source, message.id, r) }.takeIf { thread.hosted })
        }
        for (reply in thread.replies) {
            Divider()
            ReplyRow(reply, workbench, writing = reply.id == writing?.id)
        }
        Divider()
        CardFooter(fill) {
            when {
                guest -> {
                    Button(stringResource(Res.string.thread_adopt), { controller.adopt(thread); views.dismissed += thread.id }, kind = ButtonKind.Quiet, icon = StretchIcons.Pen)
                    Button(stringResource(Res.string.thread_dismiss), { views.dismissed += thread.id }, kind = ButtonKind.Quiet, icon = StretchIcons.Close)
                }
                writing != null -> {
                    ReplyActions(writing.id, controller)
                    Spacer(Modifier.weight(1f))
                    ResolveButton(thread, controller)
                }
                else -> {
                    if (thread.hosted) Button(stringResource(Res.string.thread_reply), { controller.startReply(thread) }, kind = ButtonKind.Quiet, icon = ArcIcons.Comment)
                    ResolveButton(thread, controller)
                    BasicText(
                        thread.link.orEmpty(),
                        Modifier.weight(1f).padding(end = StretchSpacing.space6),
                        style = Stretch.type.bodySm.merge(TextStyle(color = c.inkMuted, textAlign = TextAlign.End)),
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

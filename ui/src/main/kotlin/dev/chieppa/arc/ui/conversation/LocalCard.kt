package dev.chieppa.arc.ui.conversation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.chieppa.arc.core.conversation.ReviewThread
import dev.chieppa.arc.core.conversation.ThreadState
import dev.chieppa.arc.design.ArcIcons
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.comment_delete
import dev.chieppa.arc.design.generated.comment_placeholder
import dev.chieppa.arc.design.generated.comment_stage
import dev.chieppa.arc.design.generated.comment_you
import dev.chieppa.arc.design.generated.note_done
import dev.chieppa.arc.design.generated.note_edit
import dev.chieppa.arc.design.generated.note_make_comment
import dev.chieppa.arc.design.generated.note_placeholder
import dev.chieppa.arc.design.generated.thread_keep_as_note
import dev.chieppa.arc.design.generated.thread_local
import dev.chieppa.arc.ui.util.ArcSizes
import dev.chieppa.arc.ui.workbench.Workbench
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
 * The person's own comment or note under its line. A draft is written in place and staged, or kept as a note; a note
 * is edited in place, made a comment, or deleted. Both stay on this machine.
 */
@Composable
fun LocalCard(thread: ReviewThread, workbench: Workbench, modifier: Modifier = Modifier) {
    val comment = thread.local ?: return
    val c = Stretch.colors
    val controller = workbench.comments.controller
    val views = workbench.threadViews
    val writing = comment.id in controller.writing
    val expanded = writing || views.expanded(thread)
    val note = thread.state == ThreadState.Note
    val you = stringResource(Res.string.comment_you)
    Column(
        modifier
            .padding(horizontal = StretchSpacing.space24, vertical = StretchSpacing.space6)
            .widthIn(max = ArcSizes.threadMaxWidth)
            .fillMaxWidth()
            .framedSurface(Stretch.shapes.cardShape, c.surfaceFloat, c.lineStrong, Stretch.stroke),
    ) {
        ThreadHead(thread, expanded, { views.toggle(thread) }, stringResource(Res.string.thread_local), comment.body.takeIf { it.isNotBlank() }?.let { "$you: $it" })
        if (!expanded) return@Column
        Divider()
        if (writing) {
            Row(Modifier.fillMaxWidth().padding(StretchSpacing.space10), horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space8)) {
                Avatar(controller.viewer(comment.source) ?: you, null, workbench.comments.avatars)
                Column(Modifier.weight(1f)) {
                    CommentComposer(
                        comment.body,
                        { controller.write(comment.id, it) },
                        stringResource(if (note) Res.string.note_placeholder else Res.string.comment_placeholder),
                        { if (note) controller.close(comment.id) else controller.stage(comment.id) },
                        { controller.close(comment.id) },
                    )
                }
            }
        } else {
            ThreadMessageRow(thread.messages.first(), author = you)
        }
        Divider()
        CardFooter(c.surfaceFloat) {
            when {
                !note -> {
                    Button(stringResource(Res.string.comment_stage), { controller.stage(comment.id) }, kind = ButtonKind.Primary, icon = ArcIcons.Stage)
                    SubmitKeys()
                    Button(stringResource(Res.string.thread_keep_as_note), { controller.keepAsNote(comment.id) }, kind = ButtonKind.Quiet, icon = ArcIcons.Note)
                }
                writing -> {
                    Button(stringResource(Res.string.note_done), { controller.close(comment.id) }, kind = ButtonKind.Primary, icon = StretchIcons.Check)
                    SubmitKeys()
                }
                else -> {
                    Button(stringResource(Res.string.note_make_comment), { controller.makeComment(comment.id) }, kind = ButtonKind.Quiet, icon = ArcIcons.Comment)
                    Button(stringResource(Res.string.note_edit), { controller.edit(comment.id) }, kind = ButtonKind.Quiet, icon = StretchIcons.Pen)
                }
            }
            Spacer(Modifier.weight(1f))
            IconButton(ArcIcons.Delete, stringResource(Res.string.comment_delete), { controller.discard(comment.id) }, size = StretchSizes.seg)
        }
    }
}

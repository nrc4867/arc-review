package dev.chieppa.arc.ui.conversation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.chieppa.arc.core.conversation.LocalComment
import dev.chieppa.arc.core.conversation.LocalKind
import dev.chieppa.arc.core.conversation.ThreadMessage
import dev.chieppa.arc.design.ArcIcons
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.comment_cancel
import dev.chieppa.arc.design.generated.comment_you
import dev.chieppa.arc.design.generated.reply_placeholder
import dev.chieppa.arc.design.generated.reply_stage
import dev.chieppa.arc.design.generated.staged_edit
import dev.chieppa.arc.design.generated.staged_state
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.theme.StretchIcons
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.Button
import dev.chieppa.stretch.ui.ButtonKind
import dev.chieppa.arc.ui.util.TipIconButton
import org.jetbrains.compose.resources.stringResource

/**
 * The person's own reply under a thread: being written, in a text area with their avatar, or staged, marked so, with
 * a button that takes it back to writing.
 */
@Composable
fun ReplyRow(reply: LocalComment, workbench: Workbench, writing: Boolean) {
    val controller = workbench.comments.controller
    val you = stringResource(Res.string.comment_you)
    val me = ownAuthor(workbench, reply.source)
    if (writing) {
        Row(Modifier.fillMaxWidth().padding(StretchSpacing.space10), horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space8)) {
            Avatar(me.name, me.avatar, workbench.comments.avatars)
            Column(Modifier.weight(1f)) {
                CommentComposer(reply.body, { controller.write(reply.id, it) }, stringResource(Res.string.reply_placeholder), { controller.stage(reply.id) }, { controller.close(reply.id) })
            }
        }
        return
    }
    if (reply.kind != LocalKind.Staged && reply.body.isBlank()) return
    ThreadMessageRow(ThreadMessage(me.name, reply.body, reply.writtenAt, id = reply.id, avatar = me.avatar), workbench, author = you) {
        StateChip(stringResource(Res.string.staged_state), ArcIcons.Stage, stagedLook(reply.failed))
        if (!reply.fromPending) TipIconButton(StretchIcons.Pen, stringResource(Res.string.staged_edit), { controller.edit(reply.id) })
    }
}

/** The buttons under a reply being written: stage it or stop writing it. */
@Composable
fun RowScope.ReplyActions(id: String, controller: ConversationController) {
    SubmitButton(stringResource(Res.string.reply_stage), { controller.stage(id) }, ArcIcons.Stage)
    Button(stringResource(Res.string.comment_cancel), { controller.discard(id) }, kind = ButtonKind.Quiet)
}

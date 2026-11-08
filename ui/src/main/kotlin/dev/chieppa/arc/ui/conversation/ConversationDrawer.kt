package dev.chieppa.arc.ui.conversation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.chieppa.arc.core.conversation.ConversationEntry
import dev.chieppa.arc.core.conversation.LocalKind
import dev.chieppa.arc.core.conversation.Reaction
import dev.chieppa.arc.core.conversation.ThreadMessage
import dev.chieppa.arc.core.conversation.ThreadState
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.design.ArcIcons
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.comment_you
import dev.chieppa.arc.design.generated.conversation_author
import dev.chieppa.arc.design.generated.staged_edit
import dev.chieppa.arc.design.generated.staged_goes_up
import dev.chieppa.arc.design.generated.staged_state
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.theme.StretchIcons
import dev.chieppa.stretch.theme.StretchSizes
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.Divider
import dev.chieppa.stretch.ui.IconButton
import org.jetbrains.compose.resources.stringResource

/**
 * The Conversation drawer for the active pull request: its description, then its comments and review summaries oldest
 * first with their reactions, then the person's own conversation comments staged to go up with the review, and at the
 * foot a box to write one. Plain for a source that is not a pull request, and until its conversation is read.
 */
@Composable
fun ConversationDrawer(workbench: Workbench) {
    val source = workbench.state.active as? Source.PullRequest ?: return
    val controller = workbench.comments.controller
    val read = controller.hosted[source]?.conversation ?: return
    val images = workbench.comments.avatars
    val react = { id: String -> { r: Reaction -> controller.react(source, id, r) } }
    val you = stringResource(Res.string.comment_you)
    Column(Modifier.fillMaxSize()) {
        SourceHeading(workbench.state, source, Modifier.padding(StretchSpacing.space12)) {
            ConversationMeta(read)
        }
        Divider()
        Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())) {
            read.description?.let { description ->
                ConversationItem(description, images, react(description.id), panel = true) { ChipText(stringResource(Res.string.conversation_author)) }
                Divider()
            }
            for (entry in read.entries) {
                when (entry) {
                    is ConversationEntry.Comment -> ConversationItem(entry.message, images, react(entry.message.id))
                    is ConversationEntry.Review -> ConversationItem(entry.message, images, react(entry.message.id)) { ReviewChips(entry) }
                }
                Divider()
            }
            for (comment in controller.local.of(source).filter { it.anchor == null && !it.isReply && it.kind == LocalKind.Staged }) {
                val message = ThreadMessage(controller.viewer(source) ?: you, comment.body, comment.writtenAt)
                ConversationItem(message, images, null, author = you, when_ = stringResource(Res.string.staged_goes_up)) {
                    StateChip(stringResource(Res.string.staged_state), ArcIcons.Stage, stateLook(ThreadState.Staged))
                    IconButton(StretchIcons.Pen, stringResource(Res.string.staged_edit), { controller.edit(comment.id) }, size = StretchSizes.seg)
                }
                Divider()
            }
        }
        Divider()
        ConversationComposer(workbench, source)
    }
}

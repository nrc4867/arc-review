package dev.chieppa.arc.ui.conversation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import dev.chieppa.arc.core.conversation.ConversationTimeline
import dev.chieppa.arc.core.conversation.LocalKind
import dev.chieppa.arc.core.conversation.ThreadMessage
import dev.chieppa.arc.core.conversation.ThreadState
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.design.ArcIcons
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.comment_you
import dev.chieppa.arc.design.generated.staged_edit
import dev.chieppa.arc.design.generated.staged_goes_up
import dev.chieppa.arc.design.generated.staged_state
import dev.chieppa.arc.ui.util.TipIconButton
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.theme.StretchIcons
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.Divider
import org.jetbrains.compose.resources.stringResource

/**
 * The conversation of pull request [source], in the drawer or a tab ([inTab]): its heading, the row that steps,
 * groups and folds it, its items oldest first or by file, the person's own conversation comments staged to go up with
 * the review, and at the foot a box to write one. Plain until the conversation is read.
 */
@Composable
fun ConversationPanel(workbench: Workbench, source: Source.PullRequest, inTab: Boolean, modifier: Modifier = Modifier) {
    val controller = workbench.comments.controller
    val read = controller.hosted[source]?.conversation ?: return
    val grouping = workbench.conversationViews.grouping
    val items = remember(read, grouping) {
        if (grouping == ConversationGrouping.Timeline) ConversationTimeline.inOrder(read) else ConversationTimeline.byFile(read)
    }
    val list = rememberLazyListState()
    val you = stringResource(Res.string.comment_you)
    val me = ownAuthor(workbench, source)
    val own = controller.local.of(source).filter { it.anchor == null && !it.isReply && it.kind == LocalKind.Staged }
    Column(modifier.fillMaxSize()) {
        SourceHeading(workbench.state, source, Modifier.padding(StretchSpacing.space12)) { ConversationMeta(read) }
        ConversationToolbar(workbench, source, items, list, inTab)
        Divider()
        LazyColumn(Modifier.weight(1f).fillMaxWidth(), state = list) {
            items(items, key = { it.key }) { item ->
                ConversationRow(item, source, workbench)
                Divider()
            }
            items(own, key = { it.id }) { comment ->
                val message = ThreadMessage(me.name, comment.body, comment.writtenAt, avatar = me.avatar)
                ConversationItem(message, comment.id, workbench, null, author = you, when_ = stringResource(Res.string.staged_goes_up)) {
                    StateChip(stringResource(Res.string.staged_state), ArcIcons.Stage, stateLook(ThreadState.Staged))
                    TipIconButton(StretchIcons.Pen, stringResource(Res.string.staged_edit), { controller.edit(comment.id) })
                }
                Divider()
            }
        }
        Divider()
        ConversationComposer(workbench, source)
    }
}

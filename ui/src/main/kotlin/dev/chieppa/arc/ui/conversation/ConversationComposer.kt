package dev.chieppa.arc.ui.conversation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.design.ArcIcons
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.conversation_placeholder
import dev.chieppa.arc.design.generated.conversation_stage
import dev.chieppa.arc.design.generated.thread_keep_as_note
import dev.chieppa.arc.ui.util.SourceLabels
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.Button
import dev.chieppa.stretch.ui.Tooltip
import dev.chieppa.stretch.frame.caps
import dev.chieppa.arc.ui.actions.ArcKeys
import dev.chieppa.arc.ui.util.TipIconButton
import org.jetbrains.compose.resources.stringResource

/**
 * The foot of the Conversation drawer: a comment on the pull request as a whole, staged to go up with the review or
 * kept as a note on it. What is written stays in the workspace between runs.
 */
@Composable
fun ConversationComposer(workbench: Workbench, source: Source) {
    val controller = workbench.comments.controller
    val draft = controller.conversationDraft(source)
    Column(Modifier.fillMaxWidth().padding(StretchSpacing.space12), verticalArrangement = Arrangement.spacedBy(StretchSpacing.space8)) {
        val stage = { draft?.let { controller.stage(it.id) }; Unit }
        CommentComposer(
            draft?.body.orEmpty(),
            { controller.writeConversation(source, it) },
            stringResource(Res.string.conversation_placeholder, SourceLabels.of(source)),
            stage,
            {},
            focus = false,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space8), verticalAlignment = Alignment.CenterVertically) {
            Tooltip(stringResource(Res.string.conversation_stage), ArcKeys.SUBMIT_COMMENT.caps()) { Button(stringResource(Res.string.conversation_stage), stage, icon = ArcIcons.Stage) }
            Spacer(Modifier.weight(1f))
            TipIconButton(ArcIcons.Note, stringResource(Res.string.thread_keep_as_note), { draft?.let { controller.keepAsNote(it.id) } })
        }
    }
}

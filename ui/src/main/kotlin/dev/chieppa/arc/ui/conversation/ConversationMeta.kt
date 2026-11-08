package dev.chieppa.arc.ui.conversation

import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import dev.chieppa.arc.core.conversation.ConversationEntry
import dev.chieppa.arc.core.conversation.PullConversation
import dev.chieppa.arc.core.conversation.ThreadState
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.conversation_comments
import dev.chieppa.arc.design.generated.conversation_opened
import dev.chieppa.arc.design.generated.conversation_threads_open
import dev.chieppa.arc.design.generated.staged_separator
import dev.chieppa.stretch.theme.Stretch
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/** Under the Conversation drawer's heading: who opened the pull request and when, how many comments, how many threads are open. */
@Composable
fun ConversationMeta(conversation: PullConversation) {
    val parts = buildList {
        conversation.description?.let { add(stringResource(Res.string.conversation_opened, it.author, ageText(it.writtenAt))) }
        val comments = conversation.entries.count { it is ConversationEntry.Comment }
        add(pluralStringResource(Res.plurals.conversation_comments, comments, comments))
        val open = conversation.threads.count { it.state == ThreadState.Open || it.state == ThreadState.Addressed }
        add(pluralStringResource(Res.plurals.conversation_threads_open, open, open))
    }
    BasicText(parts.joinToString(stringResource(Res.string.staged_separator)), style = Stretch.type.bodySm.merge(TextStyle(color = Stretch.colors.inkMuted)), maxLines = 1)
}

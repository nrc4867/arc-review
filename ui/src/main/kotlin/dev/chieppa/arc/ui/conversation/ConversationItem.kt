package dev.chieppa.arc.ui.conversation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import dev.chieppa.arc.core.conversation.Reaction
import dev.chieppa.arc.core.conversation.ThreadMessage
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchSpacing

/**
 * One entry in a conversation: its author's picture and name, [chips] such as a verdict, how long ago, what it says
 * and its reactions, which [onReact] toggles, then [below] it, such as a review's threads. A click on its head folds it
 * to that one line with the start of what it says, under [key]. [panel] sets the pull request's description apart.
 */
@Composable
fun ConversationItem(
    message: ThreadMessage,
    key: String,
    workbench: Workbench,
    onReact: ((Reaction) -> Unit)?,
    modifier: Modifier = Modifier,
    author: String = message.author,
    panel: Boolean = false,
    when_: String? = null,
    chips: @Composable RowScope.() -> Unit = {},
    below: @Composable ColumnScope.() -> Unit = {},
) {
    val c = Stretch.colors
    val views = workbench.conversationViews
    val folded = views.isFolded(key)
    Column(
        modifier.fillMaxWidth().background(if (panel) c.surfacePanel else Color.Transparent).padding(horizontal = StretchSpacing.space12, vertical = StretchSpacing.space10),
        verticalArrangement = Arrangement.spacedBy(StretchSpacing.space6),
    ) {
        Row(
            Modifier.fillMaxWidth().clickable { views.toggleFold(key) },
            horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space8),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Avatar(message.author, message.avatar, workbench.comments.avatars, guest = message.guest)
            BasicText(author, style = Stretch.type.bodySm.merge(TextStyle(color = c.inkStrong, fontWeight = FontWeight.SemiBold)), maxLines = 1)
            chips()
            if (folded) {
                val peek = message.body.lineSequence().firstOrNull { it.isNotBlank() }.orEmpty()
                BasicText(peek, Modifier.weight(1f), style = Stretch.type.bodySm.merge(TextStyle(color = c.inkMuted)), maxLines = 1, overflow = TextOverflow.Ellipsis)
            } else {
                Spacer(Modifier.weight(1f))
            }
            BasicText(when_ ?: ageText(message.writtenAt), style = Stretch.type.bodySm.merge(TextStyle(color = c.inkMuted)), maxLines = 1)
        }
        if (folded) return@Column
        if (message.body.isNotBlank()) ClampedBody(key, message.body, workbench)
        ReactionRow(message.reactions, onReact.takeIf { message.id.isNotEmpty() })
        below()
    }
}

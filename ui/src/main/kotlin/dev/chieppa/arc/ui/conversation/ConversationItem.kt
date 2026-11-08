package dev.chieppa.arc.ui.conversation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import dev.chieppa.arc.core.conversation.Reaction
import dev.chieppa.arc.core.conversation.ThreadMessage
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchSpacing

/**
 * One entry in the Conversation drawer: its author's picture and name, [chips] such as a verdict, how long ago, what
 * it says and its reactions, which [onReact] toggles. [panel] sets the pull request's description apart.
 */
@Composable
fun ConversationItem(
    message: ThreadMessage,
    images: AvatarImages,
    onReact: ((Reaction) -> Unit)?,
    modifier: Modifier = Modifier,
    author: String = message.author,
    panel: Boolean = false,
    when_: String? = null,
    chips: @Composable RowScope.() -> Unit = {},
) {
    val c = Stretch.colors
    Column(
        modifier.fillMaxWidth().background(if (panel) c.surfacePanel else Color.Transparent).padding(horizontal = StretchSpacing.space12, vertical = StretchSpacing.space10),
        verticalArrangement = Arrangement.spacedBy(StretchSpacing.space6),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space8), verticalAlignment = Alignment.CenterVertically) {
            Avatar(message.author, message.avatar, images, guest = message.guest)
            BasicText(author, style = Stretch.type.bodySm.merge(TextStyle(color = c.inkStrong, fontWeight = FontWeight.SemiBold)), maxLines = 1)
            chips()
            Spacer(Modifier.weight(1f))
            BasicText(when_ ?: ageText(message.writtenAt), style = Stretch.type.bodySm.merge(TextStyle(color = c.inkMuted)), maxLines = 1)
        }
        if (message.body.isNotBlank()) BasicText(message.body, style = Stretch.type.body.merge(TextStyle(color = c.ink)))
        ReactionRow(message.reactions, onReact.takeIf { message.id.isNotEmpty() })
    }
}

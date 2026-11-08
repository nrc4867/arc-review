package dev.chieppa.arc.ui.conversation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import dev.chieppa.arc.core.conversation.Reaction
import dev.chieppa.arc.core.conversation.ThreadMessage
import dev.chieppa.arc.design.Arc
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.thread_guest_author
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchSpacing
import org.jetbrains.compose.resources.stringResource

/**
 * One message: who wrote it, with their picture from [images] (an agent marked as a guest, in the guest color), how
 * long ago, what, and its reactions, which [onReact] toggles when the host takes them. [author] names the writer in
 * place of the message's own, as "You" does; [trailing] sits after the time, such as a state chip.
 */
@Composable
fun ThreadMessageRow(
    message: ThreadMessage,
    modifier: Modifier = Modifier,
    images: AvatarImages? = null,
    onReact: ((Reaction) -> Unit)? = null,
    author: String = message.author,
    trailing: @Composable () -> Unit = {},
) {
    val c = Stretch.colors
    val guest = Arc.colors.guest
    Row(modifier.fillMaxWidth().padding(StretchSpacing.space10), horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space8)) {
        Avatar(message.author, message.avatar, images, guest = message.guest)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(StretchSpacing.space2)) {
            Row(horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space6), verticalAlignment = Alignment.CenterVertically) {
                BasicText(author, style = Stretch.type.bodySm.merge(TextStyle(color = if (message.guest) guest else c.inkStrong, fontWeight = FontWeight.SemiBold)))
                if (message.guest) BasicText(stringResource(Res.string.thread_guest_author), style = Stretch.type.bodySm.merge(TextStyle(color = c.inkMuted)))
                BasicText(ageText(message.writtenAt), style = Stretch.type.bodySm.merge(TextStyle(color = c.inkMuted)))
                trailing()
            }
            BasicText(message.body, style = Stretch.type.body.merge(TextStyle(color = c.ink)))
            ReactionRow(message.reactions, onReact.takeIf { message.id.isNotEmpty() })
        }
    }
}

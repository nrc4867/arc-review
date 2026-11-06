package dev.chieppa.arc.ui.conversation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import dev.chieppa.arc.core.conversation.ThreadMessage
import dev.chieppa.arc.design.Arc
import dev.chieppa.arc.design.ArcIcons
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.thread_guest_author
import dev.chieppa.arc.ui.util.ArcSizes
import dev.chieppa.stretch.theme.Icon
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchSpacing
import org.jetbrains.compose.resources.stringResource

/** One message of a thread: who wrote it (an agent marked as a guest, in the guest color), how long ago, and what. */
@Composable
fun ThreadMessageRow(message: ThreadMessage, modifier: Modifier = Modifier) {
    val c = Stretch.colors
    val guest = Arc.colors.guest
    Row(modifier.fillMaxWidth().padding(StretchSpacing.space10), horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space8)) {
        Box(Modifier.size(ArcSizes.avatar).background(if (message.guest) Arc.colors.guestWash else c.surfaceControlHi, CircleShape), contentAlignment = Alignment.Center) {
            if (message.guest) {
                Icon(ArcIcons.Guest, guest, size = ArcSizes.ring)
            } else {
                BasicText(Initials.of(message.author), style = Stretch.type.chip.merge(TextStyle(color = c.ink, fontWeight = FontWeight.SemiBold)))
            }
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(StretchSpacing.space2)) {
            Row(horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space6), verticalAlignment = Alignment.CenterVertically) {
                BasicText(message.author, style = Stretch.type.bodySm.merge(TextStyle(color = if (message.guest) guest else c.inkStrong, fontWeight = FontWeight.SemiBold)))
                if (message.guest) BasicText(stringResource(Res.string.thread_guest_author), style = Stretch.type.bodySm.merge(TextStyle(color = c.inkMuted)))
                BasicText(ageText(message.writtenAt), style = Stretch.type.bodySm.merge(TextStyle(color = c.inkMuted)))
            }
            BasicText(message.body, style = Stretch.type.body.merge(TextStyle(color = c.ink)))
        }
    }
}

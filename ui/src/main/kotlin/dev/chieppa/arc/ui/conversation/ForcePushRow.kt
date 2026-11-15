package dev.chieppa.arc.ui.conversation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import dev.chieppa.arc.core.change.ObjectId
import dev.chieppa.arc.core.conversation.HeadForcePush
import dev.chieppa.arc.design.ArcIcons
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.conversation_force_pushed
import dev.chieppa.arc.design.generated.conversation_force_pushed_to
import dev.chieppa.arc.ui.util.ArcSizes
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.theme.Icon
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchSpacing
import org.jetbrains.compose.resources.stringResource

/** A force push in the conversation, on one line: who pushed, from which commit to which, and how long ago. */
@Composable
fun ForcePushRow(push: HeadForcePush, workbench: Workbench, modifier: Modifier = Modifier) {
    val c = Stretch.colors
    val small = Stretch.type.bodySm
    val before = push.before?.take(ObjectId.SHORT_LENGTH)
    val after = push.after?.take(ObjectId.SHORT_LENGTH).orEmpty()
    val text = if (before != null) {
        stringResource(Res.string.conversation_force_pushed, push.actor, before, after)
    } else {
        stringResource(Res.string.conversation_force_pushed_to, push.actor, after)
    }
    Row(
        modifier.fillMaxWidth().padding(horizontal = StretchSpacing.space12, vertical = StretchSpacing.space8),
        horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space8),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(ArcIcons.Force, c.inkMuted, size = ArcSizes.pullIcon)
        Avatar(push.actor, push.avatar, workbench.comments.avatars)
        BasicText(text, Modifier.weight(1f), style = small.merge(TextStyle(color = c.inkMuted)), maxLines = 1, overflow = TextOverflow.Ellipsis)
        BasicText(ageText(push.at), style = small.merge(TextStyle(color = c.inkMuted)), maxLines = 1)
    }
}

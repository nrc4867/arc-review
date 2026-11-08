package dev.chieppa.arc.ui.conversation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextStyle
import dev.chieppa.arc.core.conversation.ReactionGroup
import dev.chieppa.arc.ui.util.ArcSizes
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.Tooltip

/** One reaction under a message: its emoji and how many left it, filled when the person is one of them. A click toggles theirs. */
@Composable
fun ReactionPill(group: ReactionGroup, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    val c = Stretch.colors
    val shape = Stretch.shapes.pillShape
    val name = ReactionLooks.name(group.reaction)
    Tooltip(name) {
        Row(
            modifier
                .height(ArcSizes.reactionPill)
                .clip(shape)
                .background(if (group.mine) c.onFill else Color.Transparent)
                .border(Stretch.stroke, if (group.mine) c.chipOnEdge else c.chipEdge, shape)
                .clickable(role = Role.Checkbox, onClick = onToggle)
                .semantics { stateDescription = name }
                .padding(horizontal = StretchSpacing.space6),
            horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space4),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BasicText(ReactionLooks.emoji(group.reaction), style = Stretch.type.chip)
            BasicText(group.count.toString(), style = Stretch.type.chip.merge(TextStyle(color = if (group.mine) c.onInk else c.inkControl)))
        }
    }
}

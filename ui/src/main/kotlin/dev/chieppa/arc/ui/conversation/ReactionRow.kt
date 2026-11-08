package dev.chieppa.arc.ui.conversation

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import dev.chieppa.arc.core.conversation.Reaction
import dev.chieppa.arc.core.conversation.ReactionGroup
import dev.chieppa.arc.core.conversation.Reactions
import dev.chieppa.arc.design.ArcIcons
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.reaction_add
import dev.chieppa.arc.ui.util.ArcSizes
import dev.chieppa.stretch.frame.Popover
import dev.chieppa.stretch.theme.Icon
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.Tooltip
import org.jetbrains.compose.resources.stringResource

/**
 * The reactions under a message, one pill each, and the button that opens the eight to pick from. Without
 * [onToggle] the message takes no reactions here, as one only on this machine, and the row shows what there is.
 */
@Composable
fun ReactionRow(reactions: List<ReactionGroup>, onToggle: ((Reaction) -> Unit)?, modifier: Modifier = Modifier) {
    if (onToggle == null && reactions.isEmpty()) return
    FlowRow(
        modifier.padding(top = StretchSpacing.space6),
        horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space4),
        verticalArrangement = Arrangement.spacedBy(StretchSpacing.space4),
    ) {
        for (group in reactions) ReactionPill(group, { onToggle?.invoke(group.reaction) })
        if (onToggle != null) AddReaction(reactions, onToggle)
    }
}

@Composable
private fun AddReaction(reactions: List<ReactionGroup>, onToggle: (Reaction) -> Unit) {
    val c = Stretch.colors
    var open by remember { mutableStateOf(false) }
    val label = stringResource(Res.string.reaction_add)
    Box {
        Tooltip(label) {
            Box(
                Modifier
                    .size(ArcSizes.reactionPill)
                    .clip(Stretch.shapes.pillShape)
                    .border(Stretch.stroke, c.chipEdge, Stretch.shapes.pillShape)
                    .clickable(role = Role.Button, onClickLabel = label) { open = true },
                contentAlignment = Alignment.Center,
            ) { Icon(ArcIcons.Reaction, c.inkMuted, size = ArcSizes.smallIcon) }
        }
        Popover(open, { open = false }, width = ArcSizes.reactionPicker) {
            Row(horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space2)) {
                for (reaction in Reaction.entries) {
                    val mine = Reactions.isMine(reactions, reaction)
                    Tooltip(ReactionLooks.name(reaction)) {
                        Box(
                            Modifier
                                .size(ArcSizes.reactionChoice)
                                .clip(Stretch.shapes.segShape)
                                .then(if (mine) Modifier.border(Stretch.stroke, c.chipOnEdge, Stretch.shapes.segShape) else Modifier)
                                .clickable(role = Role.Checkbox) {
                                    open = false
                                    onToggle(reaction)
                                },
                            contentAlignment = Alignment.Center,
                        ) { DisableSelection { BasicText(ReactionLooks.emoji(reaction), style = Stretch.type.body) } }
                    }
                }
            }
        }
    }
}

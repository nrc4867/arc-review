package dev.chieppa.arc.ui.conversation

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
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
import dev.chieppa.arc.ui.util.TipIconButton
import dev.chieppa.stretch.frame.Popover
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.Tooltip
import org.jetbrains.compose.resources.stringResource

/**
 * The button at the end of a message's head that opens the eight reactions to pick from, the person's own marked;
 * a pick toggles theirs through [onToggle].
 */
@Composable
fun AddReaction(reactions: List<ReactionGroup>, onToggle: (Reaction) -> Unit) {
    val c = Stretch.colors
    var open by remember { mutableStateOf(false) }
    Box {
        TipIconButton(ArcIcons.Reaction, stringResource(Res.string.reaction_add), { open = true })
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

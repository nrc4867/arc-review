package dev.chieppa.arc.ui.conversation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.chieppa.arc.core.conversation.Reaction
import dev.chieppa.arc.core.conversation.ReactionGroup
import dev.chieppa.stretch.theme.StretchSpacing

/**
 * The reactions under a message's text, one pill each from its start, nothing when it has none; the button to add
 * one is [AddReaction] in the message's head. Without [onToggle] the message takes no reactions here, as one only on
 * this machine, and the pills show what there is.
 */
@Composable
fun ReactionRow(reactions: List<ReactionGroup>, onToggle: ((Reaction) -> Unit)?, modifier: Modifier = Modifier) {
    if (reactions.isEmpty()) return
    FlowRow(
        modifier,
        horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space4),
        verticalArrangement = Arrangement.spacedBy(StretchSpacing.space4),
    ) {
        for (group in reactions) ReactionPill(group, { onToggle?.invoke(group.reaction) })
    }
}

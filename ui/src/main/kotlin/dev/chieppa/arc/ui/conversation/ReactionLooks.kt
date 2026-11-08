package dev.chieppa.arc.ui.conversation

import androidx.compose.runtime.Composable
import dev.chieppa.arc.core.conversation.Reaction
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.reaction_confused
import dev.chieppa.arc.design.generated.reaction_eyes
import dev.chieppa.arc.design.generated.reaction_heart
import dev.chieppa.arc.design.generated.reaction_hooray
import dev.chieppa.arc.design.generated.reaction_laugh
import dev.chieppa.arc.design.generated.reaction_rocket
import dev.chieppa.arc.design.generated.reaction_thumbs_down
import dev.chieppa.arc.design.generated.reaction_thumbs_up
import org.jetbrains.compose.resources.stringResource

/** How each reaction shows: the emoji GitHub shows for it, and its name for tooltips and screen readers. */
object ReactionLooks {
    fun emoji(reaction: Reaction): String = when (reaction) {
        Reaction.ThumbsUp -> "👍"
        Reaction.ThumbsDown -> "👎"
        Reaction.Laugh -> "😄"
        Reaction.Hooray -> "🎉"
        Reaction.Confused -> "😕"
        Reaction.Heart -> "❤️"
        Reaction.Rocket -> "🚀"
        Reaction.Eyes -> "👀"
    }

    @Composable
    fun name(reaction: Reaction): String = stringResource(
        when (reaction) {
            Reaction.ThumbsUp -> Res.string.reaction_thumbs_up
            Reaction.ThumbsDown -> Res.string.reaction_thumbs_down
            Reaction.Laugh -> Res.string.reaction_laugh
            Reaction.Hooray -> Res.string.reaction_hooray
            Reaction.Confused -> Res.string.reaction_confused
            Reaction.Heart -> Res.string.reaction_heart
            Reaction.Rocket -> Res.string.reaction_rocket
            Reaction.Eyes -> Res.string.reaction_eyes
        },
    )
}

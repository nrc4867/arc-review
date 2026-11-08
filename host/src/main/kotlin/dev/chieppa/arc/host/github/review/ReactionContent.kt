package dev.chieppa.arc.host.github.review

import dev.chieppa.arc.core.conversation.Reaction

/** GitHub's `ReactionContent` names for each [Reaction]. */
object ReactionContent {
    private val NAMES = mapOf(
        Reaction.ThumbsUp to "THUMBS_UP",
        Reaction.ThumbsDown to "THUMBS_DOWN",
        Reaction.Laugh to "LAUGH",
        Reaction.Hooray to "HOORAY",
        Reaction.Confused to "CONFUSED",
        Reaction.Heart to "HEART",
        Reaction.Rocket to "ROCKET",
        Reaction.Eyes to "EYES",
    )
    private val REACTIONS = NAMES.entries.associate { (reaction, name) -> name to reaction }

    fun name(reaction: Reaction): String = NAMES.getValue(reaction)

    /** The reaction GitHub calls [name], or null for one Arc does not know. */
    fun parse(name: String?): Reaction? = name?.let(REACTIONS::get)
}

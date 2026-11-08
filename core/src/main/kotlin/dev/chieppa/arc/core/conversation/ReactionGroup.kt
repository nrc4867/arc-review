package dev.chieppa.arc.core.conversation

/** How many people left [reaction] on a message, and whether the person is one of them. */
data class ReactionGroup(val reaction: Reaction, val count: Int, val mine: Boolean = false) {
    /** This group with the person's own reaction added or taken away. */
    fun toggled(): ReactionGroup = if (mine) copy(count = count - 1, mine = false) else copy(count = count + 1, mine = true)
}

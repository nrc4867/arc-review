package dev.chieppa.arc.core.conversation

/** Changing a message's reactions as the person toggles their own. */
object Reactions {
    /** [groups] with the person's [reaction] added, or taken away when it is there; empty groups are left out. */
    fun toggle(groups: List<ReactionGroup>, reaction: Reaction): List<ReactionGroup> {
        val existing = groups.firstOrNull { it.reaction == reaction }
        val changed = existing?.toggled() ?: ReactionGroup(reaction, 1, mine = true)
        val others = groups.filter { it.reaction != reaction }
        return (others + changed).filter { it.count > 0 }.sortedBy { it.reaction.ordinal }
    }

    /** Whether the person has left [reaction] among [groups]. */
    fun isMine(groups: List<ReactionGroup>, reaction: Reaction): Boolean = groups.any { it.reaction == reaction && it.mine }
}

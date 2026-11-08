package dev.chieppa.arc.core.start

/**
 * What the person changed on one start screen: the cards they [hidden], the cards they [shown] again over a
 * contributor's hide, and where they moved cards ([moves], by card id).
 */
data class StartScreenLayout(
    val hidden: Set<String> = emptySet(),
    val shown: Set<String> = emptySet(),
    val moves: Map<String, StartMove> = emptyMap(),
) {
    /** Whether the person changed nothing here. */
    val isDefault: Boolean get() = hidden.isEmpty() && shown.isEmpty() && moves.isEmpty()
}

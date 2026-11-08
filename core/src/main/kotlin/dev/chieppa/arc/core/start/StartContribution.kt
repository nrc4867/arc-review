package dev.chieppa.arc.core.start

/**
 * What one contributor adds to the start screen it was asked about: [cards] of its own, [rows] added to other cards,
 * main [buttons], and the ids of cards to [hide] there.
 */
data class StartContribution(
    val cards: List<StartCard> = emptyList(),
    val rows: List<StartRowAddition> = emptyList(),
    val buttons: List<StartButton> = emptyList(),
    val hide: List<String> = emptyList(),
)

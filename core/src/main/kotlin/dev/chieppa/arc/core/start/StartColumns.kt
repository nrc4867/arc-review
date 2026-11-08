package dev.chieppa.arc.core.start

/**
 * What one start screen shows: the cards of each column in order, each with the rows added to it, and the main
 * buttons. [hiddenBy] names, for each card a contributor hid and the person has not shown again, who hid it.
 */
data class StartColumns(
    val main: List<StartCard> = emptyList(),
    val side: List<StartCard> = emptyList(),
    val buttons: List<StartButton> = emptyList(),
    val hiddenBy: Map<String, String> = emptyMap(),
) {
    fun of(column: StartColumn): List<StartCard> = if (column == StartColumn.Main) main else side

    /** The column [card] shows in, or null when it is not showing. */
    fun columnOf(card: String): StartColumn? = when {
        main.any { it.id == card } -> StartColumn.Main
        side.any { it.id == card } -> StartColumn.Side
        else -> null
    }
}

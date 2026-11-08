package dev.chieppa.arc.core.start

/**
 * The person's changes from a card's menu, on the screen showing [StartColumns]. A move records where every card of
 * the columns it touches now stands, so the arrangement stays as the person left it whatever contributors add later.
 */
object StartArrange {
    /** [layout] with [card] hidden. */
    fun hide(layout: StartScreenLayout, card: String): StartScreenLayout =
        layout.copy(hidden = layout.hidden + card, shown = layout.shown - card)

    /** [layout] with [card] showing again, over the person's hide and any contributor's. */
    fun show(layout: StartScreenLayout, card: String): StartScreenLayout =
        layout.copy(hidden = layout.hidden - card, shown = layout.shown + card)

    /** [layout] with [card] moved [step] places along its column of [showing]; past either end it stays. */
    fun move(layout: StartScreenLayout, showing: StartColumns, card: String, step: Int): StartScreenLayout {
        val column = showing.columnOf(card) ?: return layout
        val ids = showing.of(column).map { it.id }.toMutableList()
        val from = ids.indexOf(card)
        val to = (from + step).coerceIn(0, ids.lastIndex)
        if (to == from) return layout
        ids.removeAt(from)
        ids.add(to, card)
        return layout.copy(moves = layout.moves + placed(column, ids))
    }

    /** [layout] with [card] moved to the end of the other column of [showing]. */
    fun moveAcross(layout: StartScreenLayout, showing: StartColumns, card: String): StartScreenLayout {
        val column = showing.columnOf(card) ?: return layout
        val left = showing.of(column).map { it.id } - card
        val joined = showing.of(column.other).map { it.id } + card
        return layout.copy(moves = layout.moves + placed(column, left) + placed(column.other, joined))
    }

    /** Whether [card] can move [step] places along its column of [showing]. */
    fun canMove(showing: StartColumns, card: String, step: Int): Boolean {
        val column = showing.columnOf(card) ?: return false
        val index = showing.of(column).indexOfFirst { it.id == card } + step
        return index in showing.of(column).indices
    }

    private fun placed(column: StartColumn, ids: List<String>): Map<String, StartMove> =
        ids.withIndex().associate { (i, id) -> id to StartMove(column, i) }
}

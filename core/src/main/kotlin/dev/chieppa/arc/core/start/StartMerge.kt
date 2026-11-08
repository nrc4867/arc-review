package dev.chieppa.arc.core.start

/**
 * Merges what every contributor gives one start screen with the person's changes to it into what each column shows.
 *
 * - A card shows on the screens it names. When two contributors give the same id, the first in order keeps it.
 * - Rows added to a card go after its own rows, Arc's additions first and then by contributor id; a card left with no
 *   rows is left out, unless it is one of Arc's [slots], drawn by Arc's own composable.
 * - A card goes [StartCard.before] or [StartCard.after] the card it names in its column, else at the column's end, as
 *   it does when the named card is not there. Several cards in one spot go Arc's first, then in contributor id order.
 * - Main buttons go Arc's first, then by contributor id.
 * - A contributor's `hide` leaves a card out unless the person showed it again; a card the person hid is left out;
 *   a card the person moved goes where they put it.
 */
object StartMerge {
    fun merge(
        kind: StartScreenKind,
        contributors: List<StartContributor>,
        layout: StartScreenLayout = StartScreenLayout(),
        slots: Set<String> = emptySet(),
    ): StartColumns {
        val ordered = contributors.sortedWith(compareBy<StartContributor> { it.id != StartContributor.ARC }.thenBy { it.id })
        val added = ordered.flatMap { it.contribution.rows }.groupBy({ it.card }, { it.row })
        val cards = ordered
            .flatMap { who -> who.contribution.cards.filter { kind in it.screens }.map { Placed(who.id, it) } }
            .distinctBy { it.card.id }
            .map { it.copy(card = it.card.copy(rows = it.card.rows + added[it.card.id].orEmpty())) }
            .filter { it.card.rows.isNotEmpty() || (it.who == StartContributor.ARC && it.card.id in slots) }
        val hiddenBy = linkedMapOf<String, String>()
        for (who in ordered) for (id in who.contribution.hide) hiddenBy.putIfAbsent(id, who.id)
        val shown = cards.filter { it.card.id !in layout.hidden && (it.card.id !in hiddenBy || it.card.id in layout.shown) }
        val placed = StartColumn.entries.associateWith { column -> place(shown.filter { it.card.column == column }.map { it.card }) }
        val columns = moved(placed, layout.moves)
        val ids = cards.map { it.card.id }.toSet()
        return StartColumns(
            main = columns.getValue(StartColumn.Main),
            side = columns.getValue(StartColumn.Side),
            buttons = ordered.flatMap { it.contribution.buttons },
            hiddenBy = hiddenBy.filterKeys { it in ids && it !in layout.shown },
        )
    }

    /** [cards] of one column, in contributor order, placed by their anchors. */
    private fun place(cards: List<StartCard>): List<StartCard> {
        val byId = cards.associateBy { it.id }
        val anchored = cards.filter { anchorOf(it) != null && resolves(it, byId, emptySet()) }
        val result = cards.filter { it !in anchored }.toMutableList()
        var pending = anchored
        while (pending.isNotEmpty()) {
            val ready = pending.filter { card -> result.any { it.id == anchorOf(card) } }
            for ((anchor, group) in ready.groupBy { anchorOf(it)!! }) {
                val before = group.filter { it.before != null }
                val after = group.filter { it.before == null }
                result.addAll(result.indexOfFirst { it.id == anchor } + 1, after)
                result.addAll(result.indexOfFirst { it.id == anchor }, before)
            }
            pending = pending - ready.toSet()
        }
        return result
    }

    private fun anchorOf(card: StartCard): String? = card.before ?: card.after

    /** Whether [card]'s anchor is in its column and in turn placed, without going round in a circle. */
    private fun resolves(card: StartCard, byId: Map<String, StartCard>, seen: Set<String>): Boolean {
        val anchor = anchorOf(card) ?: return true
        if (anchor in seen || anchor == card.id) return false
        val target = byId[anchor] ?: return false
        return resolves(target, byId, seen + card.id)
    }

    /** [columns] with each moved card taken out of its place and put where the person put it. */
    private fun moved(columns: Map<StartColumn, List<StartCard>>, moves: Map<String, StartMove>): Map<StartColumn, List<StartCard>> {
        val all = columns.values.flatten()
        val moving = all.filter { it.id in moves }
        return StartColumn.entries.associateWith { column ->
            val stay = columns.getValue(column).filter { it.id !in moves }.toMutableList()
            moving.filter { moves.getValue(it.id).column == column }
                .sortedWith(compareBy<StartCard> { moves.getValue(it.id).order }.thenBy { it.id })
                .forEach { card -> stay.add(moves.getValue(card.id).order.coerceIn(0, stay.size), card) }
            stay
        }
    }

    private data class Placed(val who: String, val card: StartCard)
}

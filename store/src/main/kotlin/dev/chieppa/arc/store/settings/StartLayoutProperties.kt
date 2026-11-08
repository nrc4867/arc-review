package dev.chieppa.arc.store.settings

import dev.chieppa.arc.core.start.StartColumn
import dev.chieppa.arc.core.start.StartLayout
import dev.chieppa.arc.core.start.StartMove
import dev.chieppa.arc.core.start.StartScreenKind
import dev.chieppa.arc.core.start.StartScreenLayout
import java.util.Properties

/**
 * The start screens' layout as properties: per screen, the hidden and shown card ids as comma-separated lists, and the
 * moves as `id:column:order` items. An item that does not read is left out.
 */
internal object StartLayoutProperties {
    private const val PREFIX = "start."
    private const val HIDDEN = ".hidden"
    private const val SHOWN = ".shown"
    private const val MOVES = ".moves"
    private const val LIST = ","
    private const val PART = ":"
    private const val PARTS = 3

    private val screenKeys = mapOf(
        StartScreenKind.NothingOpen to "nothing-open",
        StartScreenKind.OneSource to "one-source",
        StartScreenKind.SeveralSources to "several-sources",
    )
    private val columnKeys = mapOf(StartColumn.Main to "main", StartColumn.Side to "side")

    fun read(properties: Properties): StartLayout = StartLayout(
        screenKeys.mapNotNull { (kind, key) ->
            val layout = StartScreenLayout(
                hidden = list(properties.getProperty(PREFIX + key + HIDDEN)).toSet(),
                shown = list(properties.getProperty(PREFIX + key + SHOWN)).toSet(),
                moves = list(properties.getProperty(PREFIX + key + MOVES)).mapNotNull(::move).toMap(),
            )
            if (layout.isDefault) null else kind to layout
        }.toMap(),
    )

    fun write(layout: StartLayout, properties: Properties) {
        for ((kind, screen) in layout.screens) {
            val key = PREFIX + screenKeys.getValue(kind)
            if (screen.hidden.isNotEmpty()) properties.setProperty(key + HIDDEN, screen.hidden.sorted().joinToString(LIST))
            if (screen.shown.isNotEmpty()) properties.setProperty(key + SHOWN, screen.shown.sorted().joinToString(LIST))
            if (screen.moves.isNotEmpty()) {
                val moves = screen.moves.entries.sortedBy { it.key }.joinToString(LIST) { (id, move) -> listOf(id, columnKeys.getValue(move.column), move.order).joinToString(PART) }
                properties.setProperty(key + MOVES, moves)
            }
        }
    }

    private fun list(text: String?): List<String> = text.orEmpty().split(LIST).map { it.trim() }.filter { it.isNotEmpty() }

    private fun move(item: String): Pair<String, StartMove>? {
        val parts = item.split(PART)
        if (parts.size != PARTS) return null
        val column = columnKeys.entries.firstOrNull { it.value == parts[1] }?.key ?: return null
        val order = parts[2].toIntOrNull()?.takeIf { it >= 0 } ?: return null
        return parts[0] to StartMove(column, order)
    }
}

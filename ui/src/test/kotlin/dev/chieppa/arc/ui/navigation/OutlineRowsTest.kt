package dev.chieppa.arc.ui.navigation

import dev.chieppa.arc.codeintel.model.CodeRange
import dev.chieppa.arc.codeintel.model.OutlineSymbol
import kotlin.test.Test
import kotlin.test.assertEquals

class OutlineRowsTest {
    private fun symbol(name: String, line: Int, vararg children: OutlineSymbol) = OutlineSymbol(name, "class", CodeRange.onLine(line, 0, name.length), children = children.toList())

    private val outline = listOf(
        symbol("TokenService", 1, symbol("refresh", 3), symbol("logout", 9)),
        symbol("TokenReuseException", 20),
    )

    @Test
    fun aFilterKeepsMatchesAndWhatTheyAreIn() {
        assertEquals(listOf("TokenService" to 0, "logout" to 1), OutlineRows.of(outline, "LOG").map { it.symbol.name to it.depth })
        assertEquals(4, OutlineRows.of(outline, "").size)
    }

    @Test
    fun theCurrentRowIsTheLastStartingAtOrAboveTheCaret() {
        val rows = OutlineRows.of(outline, "")
        assertEquals("refresh", OutlineRows.current(rows, 5)?.symbol?.name)
        assertEquals(null, OutlineRows.current(rows, null))
    }
}

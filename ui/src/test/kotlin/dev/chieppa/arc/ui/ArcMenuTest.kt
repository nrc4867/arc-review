package dev.chieppa.arc.ui

import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.menu_file
import dev.chieppa.arc.ui.menus.ArcMenu
import dev.chieppa.stretch.core.ActionId
import kotlin.test.Test
import kotlin.test.assertEquals

class ArcMenuTest {
    private val a = ActionId("a")
    private val b = ActionId("b")
    private val c = ActionId("c")

    @Test
    fun leavingActionsOutTidiesTheDividers() {
        val menu = ArcMenu(Res.string.menu_file, listOf(a, null, b, null, c))
        assertEquals(listOf(a, null, c), menu.without(setOf(b)).items)
        assertEquals(listOf(b, null, c), menu.without(setOf(a)).items)
        assertEquals(listOf(a, null, b), menu.without(setOf(c)).items)
        assertEquals(emptyList(), menu.without(setOf(a, b, c)).items)
    }
}

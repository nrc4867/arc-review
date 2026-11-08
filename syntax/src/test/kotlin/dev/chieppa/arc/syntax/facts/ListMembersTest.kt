package dev.chieppa.arc.syntax.facts

import kotlin.test.Test
import kotlin.test.assertEquals

class ListMembersTest {
    @Test
    fun aMemberBelongsToTheInnermostListAroundIt() {
        // f(a, g(b, c), d): the outer list spans 1..20, the inner 6..12.
        val lists = ListMembers()
        lists.addList(1, 20)
        lists.addList(6, 12)
        listOf(2 to "a", 4 to "g(b, c)", 7 to "b", 10 to "c", 15 to "d").forEach { (at, text) -> lists.addMember(at, text) }
        val owned = lists.byList()
        assertEquals(listOf("a", "g(b, c)", "d"), owned[1])
        assertEquals(listOf("b", "c"), owned[6])
    }
}

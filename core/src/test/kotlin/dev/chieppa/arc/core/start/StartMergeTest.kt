package dev.chieppa.arc.core.start

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StartMergeTest {
    private val one = setOf(StartScreenKind.OneSource)
    private val row = StartRow("row")

    private fun card(id: String, column: StartColumn = StartColumn.Main, before: String? = null, after: String? = null, rows: List<StartRow> = listOf(row), screens: Set<StartScreenKind> = one) =
        StartCard(id, id, screens, column, rows = rows, before = before, after = after)

    private fun arc(vararg cards: StartCard, buttons: List<StartButton> = emptyList()) =
        StartContributor(StartContributor.ARC, StartContribution(cards.toList(), buttons = buttons))

    private fun plugin(id: String, vararg cards: StartCard, rows: List<StartRowAddition> = emptyList(), buttons: List<StartButton> = emptyList(), hide: List<String> = emptyList()) =
        StartContributor(id, StartContribution(cards.toList(), rows, buttons, hide))

    private fun ids(cards: List<StartCard>) = cards.map { it.id }

    private val arcCards = arc(card("arc.waiting"), card("arc.notes"), card("arc.revisions", StartColumn.Side), card("arc.keys", StartColumn.Side))

    @Test
    fun arcsCardsKeepTheirOrderInEachColumn() {
        val merged = StartMerge.merge(StartScreenKind.OneSource, listOf(arcCards))
        assertEquals(listOf("arc.waiting", "arc.notes"), ids(merged.main))
        assertEquals(listOf("arc.revisions", "arc.keys"), ids(merged.side))
    }

    @Test
    fun aCardShowsOnlyOnTheScreensItNames() {
        val merged = StartMerge.merge(StartScreenKind.NothingOpen, listOf(arcCards, plugin("speckit", card("speckit.all", screens = StartScreenKind.entries.toSet()))))
        assertEquals(listOf("speckit.all"), ids(merged.main))
        assertTrue(merged.side.isEmpty())
    }

    @Test
    fun aCardGoesAfterOrBeforeTheCardItNames() {
        val merged = StartMerge.merge(
            StartScreenKind.OneSource,
            listOf(
                arcCards,
                plugin("speckit", card("speckit.specs", StartColumn.Side, after = "arc.revisions"), card("speckit.top", before = "arc.waiting")),
            ),
        )
        assertEquals(listOf("speckit.top", "arc.waiting", "arc.notes"), ids(merged.main))
        assertEquals(listOf("arc.revisions", "speckit.specs", "arc.keys"), ids(merged.side))
    }

    @Test
    fun aCardNamingNoCardOrOneNotOnTheScreenOrInAnotherColumnGoesAtTheColumnsEnd() {
        val merged = StartMerge.merge(
            StartScreenKind.OneSource,
            listOf(
                arcCards,
                plugin("b", card("b.plain")),
                plugin("a", card("a.missing", after = "arc.recent"), card("a.across", after = "arc.revisions")),
            ),
        )
        assertEquals(listOf("arc.waiting", "arc.notes", "a.missing", "a.across", "b.plain"), ids(merged.main))
    }

    @Test
    fun severalCardsInOneSpotGoInContributorIdOrderWhateverOrderTheyCameIn() {
        val merged = StartMerge.merge(
            StartScreenKind.OneSource,
            listOf(
                plugin("zeta", card("zeta.one", after = "arc.waiting")),
                arcCards,
                plugin("alpha", card("alpha.one", after = "arc.waiting"), card("alpha.two", after = "arc.waiting")),
            ),
        )
        assertEquals(listOf("arc.waiting", "alpha.one", "alpha.two", "zeta.one", "arc.notes"), ids(merged.main))
    }

    @Test
    fun aCardCanFollowAnotherContributorsCard() {
        val merged = StartMerge.merge(
            StartScreenKind.OneSource,
            listOf(arcCards, plugin("b", card("b.next", after = "a.first")), plugin("a", card("a.first", after = "arc.waiting"))),
        )
        assertEquals(listOf("arc.waiting", "a.first", "b.next", "arc.notes"), ids(merged.main))
    }

    @Test
    fun cardsNamingEachOtherInACircleGoAtTheEnd() {
        val merged = StartMerge.merge(
            StartScreenKind.OneSource,
            listOf(arcCards, plugin("p", card("p.x", after = "p.y"), card("p.y", after = "p.x"))),
        )
        assertEquals(listOf("arc.waiting", "arc.notes", "p.x", "p.y"), ids(merged.main))
    }

    @Test
    fun addedRowsGoAfterTheCardsOwnRowsArcsFirstThenByContributor() {
        val mine = StartRow("mine")
        val fromZ = StartRow("from z")
        val fromA = StartRow("from a")
        val merged = StartMerge.merge(
            StartScreenKind.OneSource,
            listOf(
                plugin("z", rows = listOf(StartRowAddition("arc.waiting", fromZ))),
                arc(card("arc.waiting", rows = listOf(mine))),
                plugin("a", rows = listOf(StartRowAddition("arc.waiting", fromA))),
            ),
        )
        assertEquals(listOf(mine, fromA, fromZ), merged.main.single().rows)
    }

    @Test
    fun aCardWithNoRowsIsLeftOutUnlessItIsAnArcSlotOrGainsRows() {
        val merged = StartMerge.merge(
            StartScreenKind.OneSource,
            listOf(
                arc(card("arc.progress", rows = emptyList()), card("arc.waiting", rows = emptyList()), card("arc.notes", rows = emptyList())),
                plugin("p", card("p.empty", rows = emptyList()), card("arc.slot-like", rows = emptyList()), rows = listOf(StartRowAddition("arc.notes", row))),
            ),
            slots = setOf("arc.progress", "arc.slot-like"),
        )
        assertEquals(listOf("arc.progress", "arc.notes"), ids(merged.main))
    }

    @Test
    fun theFirstContributorOfAnIdKeepsIt() {
        val merged = StartMerge.merge(StartScreenKind.OneSource, listOf(plugin("p", card("arc.notes", StartColumn.Side)), arcCards))
        assertEquals(listOf("arc.waiting", "arc.notes"), ids(merged.main))
        assertEquals(listOf("arc.revisions", "arc.keys"), ids(merged.side))
    }

    @Test
    fun buttonsGoArcsFirstThenByContributor() {
        val go = StartButton("Continue", StartAction("continue"))
        val z = StartButton("Z", StartAction("z.do"))
        val a = StartButton("A", StartAction("a.do"))
        val merged = StartMerge.merge(StartScreenKind.OneSource, listOf(plugin("z", buttons = listOf(z)), arc(buttons = listOf(go)), plugin("a", buttons = listOf(a))))
        assertEquals(listOf(go, a, z), merged.buttons)
    }

    @Test
    fun aContributorsHideLeavesACardOutAndSaysWhoHidIt() {
        val merged = StartMerge.merge(StartScreenKind.OneSource, listOf(arcCards, plugin("speckit", hide = listOf("arc.notes", "arc.nothing"))))
        assertEquals(listOf("arc.waiting"), ids(merged.main))
        assertEquals(mapOf("arc.notes" to "speckit"), merged.hiddenBy)
    }

    @Test
    fun thePersonShowingACardWinsOverAContributorsHide() {
        val layout = StartArrange.show(StartScreenLayout(), "arc.notes")
        val merged = StartMerge.merge(StartScreenKind.OneSource, listOf(arcCards, plugin("speckit", hide = listOf("arc.notes"))), layout)
        assertEquals(listOf("arc.waiting", "arc.notes"), ids(merged.main))
        assertTrue(merged.hiddenBy.isEmpty())
    }

    @Test
    fun aCardThePersonHidIsLeftOut() {
        val layout = StartArrange.hide(StartScreenLayout(), "arc.waiting")
        val merged = StartMerge.merge(StartScreenKind.OneSource, listOf(arcCards), layout)
        assertEquals(listOf("arc.notes"), ids(merged.main))
    }

    @Test
    fun aCardThePersonMovedStaysWhereTheyPutItOverAPluginsPlacement() {
        val contributors = listOf(arcCards, plugin("speckit", card("speckit.specs", StartColumn.Side, after = "arc.revisions")))
        val before = StartMerge.merge(StartScreenKind.OneSource, contributors)
        val layout = StartArrange.moveAcross(StartScreenLayout(), before, "speckit.specs")
        val after = StartMerge.merge(StartScreenKind.OneSource, contributors, layout)
        assertEquals(listOf("arc.waiting", "arc.notes", "speckit.specs"), ids(after.main))
        assertEquals(listOf("arc.revisions", "arc.keys"), ids(after.side))
    }

    @Test
    fun movingUpAndDownSwapsWithTheNeighbourAndStopsAtTheEnds() {
        val contributors = listOf(arc(card("arc.a"), card("arc.b"), card("arc.c")))
        var layout = StartScreenLayout()
        fun shown() = StartMerge.merge(StartScreenKind.OneSource, contributors, layout)
        layout = StartArrange.move(layout, shown(), "arc.c", -1)
        assertEquals(listOf("arc.a", "arc.c", "arc.b"), ids(shown().main))
        layout = StartArrange.move(layout, shown(), "arc.b", -1)
        assertEquals(listOf("arc.a", "arc.b", "arc.c"), ids(shown().main))
        layout = StartArrange.move(layout, shown(), "arc.a", -1)
        assertEquals(listOf("arc.a", "arc.b", "arc.c"), ids(shown().main))
        assertTrue(!StartArrange.canMove(shown(), "arc.a", -1))
        assertTrue(StartArrange.canMove(shown(), "arc.a", 1))
        layout = StartArrange.move(layout, shown(), "arc.a", 1)
        assertEquals(listOf("arc.b", "arc.a", "arc.c"), ids(shown().main))
    }

    @Test
    fun aCardAddedAfterThePersonArrangedAColumnKeepsItsDefaultPlaceAmongThem() {
        val arcOnly = listOf(arcCards)
        val layout = StartArrange.move(StartScreenLayout(), StartMerge.merge(StartScreenKind.OneSource, arcOnly), "arc.notes", -1)
        val merged = StartMerge.merge(StartScreenKind.OneSource, arcOnly + plugin("p", card("p.new")), layout)
        assertEquals(listOf("arc.notes", "arc.waiting", "p.new"), ids(merged.main))
    }

    @Test
    fun layoutChangesApplyPerScreen() {
        val all = StartScreenKind.entries.toSet()
        val contributors = listOf(arc(card("arc.keys", screens = all), card("arc.help", screens = all)))
        val layout = StartLayout().with(StartScreenKind.NothingOpen, StartArrange.hide(StartScreenLayout(), "arc.keys"))
        assertEquals(listOf("arc.help"), ids(StartMerge.merge(StartScreenKind.NothingOpen, contributors, layout.of(StartScreenKind.NothingOpen)).main))
        assertEquals(listOf("arc.keys", "arc.help"), ids(StartMerge.merge(StartScreenKind.OneSource, contributors, layout.of(StartScreenKind.OneSource)).main))
        assertEquals(StartLayout(), layout.with(StartScreenKind.NothingOpen, StartScreenLayout()))
    }

    @Test
    fun theScreenFollowsHowManySourcesAreSelected() {
        assertEquals(StartScreenKind.NothingOpen, StartScreenKind.of(0))
        assertEquals(StartScreenKind.OneSource, StartScreenKind.of(1))
        assertEquals(StartScreenKind.SeveralSources, StartScreenKind.of(3))
    }
}

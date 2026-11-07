package dev.chieppa.arc.core.pane

import dev.chieppa.arc.core.workspace.Workspace
import dev.chieppa.arc.core.source.RepoName
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PageTest {
    private val workspace = Workspace()
    private val file = Pane(PaneId("p1"), RepoName("api"), "src/A.kt", Revision("main"))

    @Test
    fun pagesAreSavedBesidePanesAndFilterPagesArePinned() {
        val page = Page.Filter(PaneId("p2"), "f1")
        val with = workspace.withPane(file).withPage(page)
        assertEquals(listOf(page), with.pages)
        assertEquals(listOf("p2"), with.pinnedIds)
        assertEquals(setOf("p1", "p2"), with.tabIds)
        assertEquals(emptyList(), with.withoutPage(page.id).pages)
    }

    @Test
    fun aPageCannotTakeAPanesId() {
        assertFailsWith<IllegalArgumentException> { workspace.withPane(file).withPage(Page.Filter(PaneId("p1"), "f1")) }
    }
}

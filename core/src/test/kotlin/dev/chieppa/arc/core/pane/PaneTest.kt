package dev.chieppa.arc.core.pane

import dev.chieppa.arc.core.source.RepoName
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PaneTest {
    private fun pane(path: String) = Pane(PaneId("p1"), RepoName("platform-docs"), path, Revision("main"), PaneView.Rendered)

    @Test
    fun aTabShowsTheFileName() {
        assertEquals("auth.md", pane("docs/hld/auth.md").fileName)
        assertEquals("README.md", pane("README.md").fileName)
    }

    @Test
    fun aPathIsRelativeToTheRepo() {
        listOf("", "/docs/a.md", "docs/").forEach { path -> assertFailsWith<IllegalArgumentException>(path) { pane(path) } }
    }

    @Test
    fun aRevisionHasNoSpaces() {
        assertFailsWith<IllegalArgumentException> { Revision("main branch") }
        assertFailsWith<IllegalArgumentException> { Revision("") }
    }
}

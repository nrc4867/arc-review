package dev.chieppa.arc.ui.review

import dev.chieppa.arc.core.pane.Page
import dev.chieppa.arc.ui.util.ArcDispatchers
import dev.chieppa.arc.ui.workbench.Workbench
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class PaneReloadTest {
    private val workbench = Workbench(dispatchers = ArcDispatchers.on(UnconfinedTestDispatcher()))

    private fun settings(): Page.Settings = workbench.state.workspace.pages.filterIsInstance<Page.Settings>().single()

    @Test
    fun reloadingTheSettingsPageOpensItOnItsFirstSection() {
        workbench.panes.openSettings()
        workbench.panes.showSettings(settings(), "arc.mcp")
        assertEquals("arc.mcp", settings().section)
        workbench.panes.reload(settings().id.value)
        assertNull(settings().section)
    }

    @Test
    fun reloadingAnotherTabChangesNothing() {
        workbench.panes.openSettings()
        workbench.panes.showSettings(settings(), "arc.mcp")
        val before = workbench.state.workspace
        workbench.panes.reload("no-such-tab")
        assertEquals(before, workbench.state.workspace)
    }
}

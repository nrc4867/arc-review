package dev.chieppa.arc.ui.mcp

import androidx.compose.ui.test.ExperimentalTestApi
import dev.chieppa.arc.core.pane.Page
import dev.chieppa.arc.mcp.dev.ManagerPluginHost
import dev.chieppa.arc.plugins.manifest.Capabilities
import dev.chieppa.arc.plugins.protocol.Contributions
import dev.chieppa.arc.plugins.protocol.PaneContribution
import dev.chieppa.arc.plugins.protocol.PanelContribution
import dev.chieppa.arc.plugins.protocol.ViewKind
import dev.chieppa.arc.ui.plugins.PluginIds
import dev.chieppa.arc.ui.plugins.PluginRig
import dev.chieppa.arc.ui.plugins.PluginRig.Companion.shows
import kotlinx.coroutines.runBlocking
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** The window as the development tools reach it: opening a plugin's panel and pane, and picturing them alone. */
@OptIn(ExperimentalTestApi::class)
class WindowDevWindowUiTest {
    private val contributions = Contributions(panels = listOf(PanelContribution("todos", "TODOs", "check")), panes = listOf(PaneContribution("console", "Command")))
    private val rig = PluginRig("""{"panels":true,"panes":true}""", contributions, Capabilities(panels = true, panes = true)) {
        """[{"op":"create","id":1,"type":"Button","props":{"label":"Count them"}},{"op":"append","parent":0,"child":1}]"""
    }

    @AfterTest
    fun cleanUp() = rig.cleanUp()

    @Test
    fun aPanelAndAPaneOpenAsThePersonWouldAndArePicturedAlone() = rig.test {
        val workbench = rig.open(this)
        val window = WindowDevWindow(workbench, workbench.mcp.captures)
        val host = ManagerPluginHost(workbench.plugins.manager!!)
        assertEquals(null, assertNotNull(runBlocking { window.context(null) }).source)
        assertNull(runBlocking { window.context("api#1") })

        runBlocking { window.open(PluginRig.ID, ViewKind.Panel, "todos") }
        waitForIdle()
        assertTrue(workbench.frameLayout.drawers.single { it.id == PluginIds.drawer(PluginRig.ID, "todos") }.open)
        assertTrue(shows("Count them"))
        val panel = host.views(PluginRig.ID).single { it.kind == ViewKind.Panel }
        assertEquals("Button", panel.nodes.single().type)
        val png = assertNotNull(runBlocking { window.capture(panel.id) })
        assertEquals(listOf(0x89, 0x50, 0x4E, 0x47), png.take(4).map { it.toInt() and 0xFF })

        runBlocking { window.open(PluginRig.ID, ViewKind.Pane, "console") }
        waitForIdle()
        assertTrue(workbench.state.workspace.pages.any { it is Page.Plugin && it.pane == "console" })
        assertNull(runBlocking { window.capture("todos/none") })
    }
}

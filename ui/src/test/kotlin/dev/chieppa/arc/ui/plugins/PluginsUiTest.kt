package dev.chieppa.arc.ui.plugins

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.input.key.Key
import dev.chieppa.arc.core.pane.Page
import dev.chieppa.arc.plugins.config.PluginsConfig
import dev.chieppa.arc.plugins.host.DiagnosticKind
import dev.chieppa.arc.plugins.manifest.Capabilities
import dev.chieppa.arc.plugins.host.PluginDiagnostic
import dev.chieppa.arc.plugins.host.Preparation
import dev.chieppa.arc.plugins.manager.PluginPreparer
import dev.chieppa.arc.plugins.protocol.ActionContribution
import dev.chieppa.arc.plugins.protocol.Contributions
import dev.chieppa.arc.plugins.protocol.Envelope
import dev.chieppa.arc.plugins.protocol.Methods
import dev.chieppa.arc.plugins.protocol.MountParams
import dev.chieppa.arc.plugins.protocol.PaneContribution
import dev.chieppa.arc.plugins.protocol.PanelContribution
import dev.chieppa.arc.plugins.protocol.ViewKind
import dev.chieppa.arc.ui.plugins.PluginRig.Companion.shows
import dev.chieppa.arc.ui.plugins.PluginRig.Companion.showsTag
import dev.chieppa.arc.ui.plugins.settings.PluginSettingsTags
import dev.chieppa.arc.ui.workbench.Workbench
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class PluginsUiTest {
    private val contributions = Contributions(
        actions = listOf(ActionContribution("run", "Run the tests")),
        panels = listOf(PanelContribution("todos", "TODOs", "check")),
        panes = listOf(PaneContribution("console", "Command", "console")),
        settingsPage = true,
    )
    private val rig = PluginRig(
        """{"panels":true,"panes":true,"actions":true,"console":true}""",
        contributions,
        Capabilities(panels = true, panes = true, actions = true, console = true),
        ::draw,
    )
    private val process = rig.process
    private val console = rig.console
    private val folder = rig.folder
    private val approved = rig.approved
    private val enabled = rig.enabled

    @AfterTest
    fun cleanUp() = rig.cleanUp()

    private fun pluginTest(body: ComposeUiTest.() -> Unit) = rig.test(body)

    private fun ComposeUiTest.open(preparer: PluginPreparer = rig.ready, config: PluginsConfig = enabled): Workbench = rig.open(this, preparer, config)

    private fun draw(mount: MountParams): String = when (mount.kind) {
        ViewKind.Panel -> """[{"op":"create","id":1,"type":"Button","props":{"label":"Count them","onClick":{"${'$'}handler":true}}},{"op":"append","parent":0,"child":1}]"""
        ViewKind.Settings -> """[{"op":"create","id":1,"type":"Text","props":{}},{"op":"text","id":2,"text":"Drawn by the plugin"},{"op":"append","parent":1,"child":2},{"op":"append","parent":0,"child":1}]"""
        ViewKind.Pane -> """[{"op":"create","id":1,"type":"Console","props":{"handle":"todos-1"}},{"op":"append","parent":0,"child":1}]"""
    }

    @Test
    fun aPanelIsADrawerWhoseButtonsSendTheirEvents() = pluginTest {
        val workbench = open()
        val drawer = PluginIds.drawer("todos", "todos")
        assertTrue(workbench.frameLayout.drawers.any { it.id == drawer })
        workbench.toggleDrawer(drawer)
        waitForIdle()
        onNodeWithText("Count them").performClick()
        waitForIdle()
        assertEquals(Methods.VIEW_EVENT, (process.received.last() as Envelope.Notification).method)
    }

    @Test
    fun anActionRunsFromTheRegistryWithItsLabel() = pluginTest {
        val workbench = open()
        val id = PluginIds.action("todos", "run")
        assertEquals("Run the tests", workbench.plugins.actions.label(id.value))
        workbench.registry[id]!!.run()
        waitForIdle()
        assertTrue(process.received.any { it is Envelope.Request && it.method == Methods.ACTION_RUN })
    }

    @Test
    fun thePluginsSectionListsThePluginWithItsOwnPageAndItsSwitch() = pluginTest {
        val workbench = open()
        workbench.panes.openSettings()
        waitForIdle()
        onNodeWithText("Plugins").performClick()
        waitForIdle()
        assertTrue(showsTag(PluginSettingsTags.PLUGIN + "todos"))
        assertTrue(shows("console") && shows("panels"))
        assertTrue(shows("Drawn by the plugin"))
        onNode(hasContentDescription("Enable TODOs")).performClick()
        waitForIdle()
        assertEquals(emptySet(), workbench.plugins.config.enabled)
        assertTrue(!shows("Drawn by the plugin"))
    }

    @Test
    fun aPluginThatDoesNotTranspileShowsItsProblemInPlace() = pluginTest {
        val problem = PluginDiagnostic(DiagnosticKind.Transpile, "Unexpected end of file", "src/index.tsx", 23, 0)
        val workbench = open({ _, _, _, _ -> Preparation.Failed(listOf(problem)) })
        workbench.panes.openSettings()
        waitForIdle()
        onNodeWithText("Plugins").performClick()
        waitForIdle()
        assertTrue(showsTag(PluginSettingsTags.PROBLEM + "todos"))
        assertTrue(shows("src/index.tsx:23") && shows("Unexpected end of file"))
        assertTrue(shows("1 of 1 plugin on") && shows("1 problem"))
    }

    @Test
    fun anActionFollowsItsPluginThroughReloadsWithItsKeys() = pluginTest {
        val workbench = open()
        val id = PluginIds.action("todos", "run")
        assertEquals(emptyList(), workbench.registry[id]!!.keys)
        process.contributions = contributions.copy(actions = listOf(ActionContribution("run", "Run the tests", keys = "Ctrl+Shift+Y")))
        workbench.plugins.manager!!.instance("todos")!!.start()
        waitForIdle()
        assertEquals(listOfNotNull(PluginIds.keys("Ctrl+Shift+Y")), workbench.registry[id]!!.keys)
        process.contributions = contributions.copy(actions = emptyList())
        workbench.plugins.manager!!.instance("todos")!!.start()
        waitForIdle()
        assertNull(workbench.registry[id])
        assertEquals(3, process.starts)
    }

    @Test
    fun anOffPluginsRiskyCapabilitiesAreWarnings() = pluginTest {
        val workbench = open(config = enabled.copy(enabled = emptySet(), approved = emptyMap()))
        workbench.panes.openSettings()
        waitForIdle()
        onNodeWithText("Plugins").performClick()
        waitForIdle()
        assertTrue(onAllNodes(hasContentDescription("runs any program as you", substring = true)).fetchSemanticsNodes().size == 1)
        assertTrue(shows("0 of 1 plugin on"))
        onNode(hasContentDescription("Enable TODOs")).performClick()
        waitForIdle()
        assertEquals(approved, workbench.plugins.config.approved["todos"])
        assertTrue(onAllNodes(hasContentDescription("runs any program as you", substring = true)).fetchSemanticsNodes().isEmpty())
        assertTrue(shows("1 of 1 plugin on"))
    }

    @Test
    fun aPaneShowsAConsoleThatTakesInput() = pluginTest {
        val workbench = open()
        workbench.plugins.manager!!.consoles.start("todos", listOf("npm", "test"), folder, { _, _ -> }, { _, _ -> })
        console.print("3 passing\nContinue? [y/N] ")
        workbench.panes.openPluginPane("todos", "console", "{}", null)
        waitForIdle()
        val page = workbench.state.workspace.pages.single() as Page.Plugin
        assertEquals("Command", workbench.plugins.tabOf(page).title)
        assertTrue(shows("3 passing"))
        assertTrue(shows("Continue? [y/N] "))
        val field = onNode(hasSetTextAction())
        field.performTextInput("y")
        field.performKeyInput { pressKey(Key.Enter) }
        waitForIdle()
        assertEquals(listOf("y"), console.typed)
    }
}

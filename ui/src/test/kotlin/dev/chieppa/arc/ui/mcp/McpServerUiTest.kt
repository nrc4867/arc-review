package dev.chieppa.arc.ui.mcp

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import dev.chieppa.arc.mcp.server.McpEndpoint
import dev.chieppa.arc.mcp.server.McpServerState
import dev.chieppa.arc.mcp.server.McpSetup
import dev.chieppa.arc.mcp.server.McpToken
import dev.chieppa.arc.mcp.tools.ArcTool
import dev.chieppa.arc.plugins.secrets.MemorySecretStore
import dev.chieppa.arc.plugins.secrets.SecretStore
import dev.chieppa.arc.ui.frame.ArcFrame
import dev.chieppa.arc.ui.plugins.PluginRig.Companion.shows
import dev.chieppa.arc.ui.util.ArcDispatchers
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.test.setStretchContent
import dev.chieppa.stretch.theme.StretchThemes
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class, ExperimentalCoroutinesApi::class)
class McpServerUiTest {
    private class FakeEndpoint : McpEndpoint {
        override val clients = MutableStateFlow(0)
        var stopped = false

        override suspend fun start(port: Int): Int = port

        override fun stop() {
            stopped = true
        }
    }

    private val endpoint = FakeEndpoint()
    private val choices = mutableListOf<Boolean>()
    private val copied = mutableListOf<String>()
    private var tools: List<ArcTool> = emptyList()

    @Test
    fun theServerIsSwitchedOnInSettingsShowsItsCommandAndItsClientsInTheStatusBar() = runComposeUiTest {
        val token = McpToken(MemorySecretStore(lasting = true))
        val setup = McpSetup(token, port = PORT, version = "test", onChange = { choices += it }, endpoint = { all, _ -> tools = all; endpoint })
        val workbench = Workbench(dispatchers = ArcDispatchers.on(UnconfinedTestDispatcher(mainClock.scheduler)), clipboard = { copied += it }, mcp = setup)
        setStretchContent(StretchThemes.WorkshopDark) { ArcFrame(workbench) }
        waitForIdle()
        assertEquals(McpServerState.Off, workbench.mcp.state)
        workbench.panes.openSettings()
        waitForIdle()
        onNodeWithText("MCP server").performClick()
        waitForIdle()
        assertTrue(shows("Off"))
        onNodeWithTag(McpSettingsTags.SWITCH).performClick()
        waitForIdle()
        assertEquals(listOf(true), choices)
        assertEquals(McpServerState.Listening(PORT, 0), workbench.mcp.state)
        val command = "claude mcp add --transport http arc http://127.0.0.1:$PORT/mcp --header \"Authorization: Bearer ${token.current()}\""
        assertTrue(shows(command))
        onNodeWithTag(McpSettingsTags.COPY).performClick()
        assertEquals(listOf(command), copied)

        endpoint.clients.value = 2
        waitForIdle()
        assertTrue(shows("2 clients connected"))
        onNodeWithTag(McpSettingsTags.SWITCH).performClick()
        waitForIdle()
        assertTrue(endpoint.stopped)
        assertEquals(listOf(true, false), choices)
        assertTrue(!shows("clients connected") && !shows(command))
    }

    @Test
    fun aKeychainThatCannotBeOpenedShowsItsReasonAndTheServerStillRuns() = runComposeUiTest {
        val unreachable = object : SecretStore {
            override val lasting = true

            override fun read(plugin: String, key: String): String? = throw IOException("Cannot open library: Security")

            override fun write(plugin: String, key: String, value: String) = throw IOException("Cannot open library: Security")

            override fun delete(plugin: String, key: String) = Unit
        }
        val setup = McpSetup(McpToken(unreachable), port = PORT, version = "test", endpoint = { _, _ -> endpoint })
        val workbench = Workbench(dispatchers = ArcDispatchers.on(UnconfinedTestDispatcher(mainClock.scheduler)), clipboard = { copied += it }, mcp = setup)
        setStretchContent(StretchThemes.WorkshopDark) { ArcFrame(workbench) }
        workbench.panes.openSettings()
        waitForIdle()
        onNodeWithText("MCP server").performClick()
        waitForIdle()
        onNodeWithTag(McpSettingsTags.SWITCH).performClick()
        waitForIdle()
        assertEquals(McpServerState.Listening(PORT, 0), workbench.mcp.state)
        onNodeWithTag(McpSettingsTags.TOKEN_PROBLEM).assertTextEquals("Keychain: Cannot open library: Security")
        assertTrue(shows("Token kept only until Arc closes"))
        onNodeWithTag(McpSettingsTags.COPY).performClick()
        assertTrue(copied.single().startsWith("claude mcp add"))
    }

    @Test
    fun withoutASetupThereIsNoSection() = runComposeUiTest {
        val workbench = Workbench(dispatchers = ArcDispatchers.on(UnconfinedTestDispatcher(mainClock.scheduler)))
        setStretchContent(StretchThemes.WorkshopDark) { ArcFrame(workbench) }
        workbench.panes.openSettings()
        waitForIdle()
        assertTrue(!shows("MCP server"))
    }

    private companion object {
        const val PORT = 47830
    }
}

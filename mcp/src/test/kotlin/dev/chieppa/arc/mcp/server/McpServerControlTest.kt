package dev.chieppa.arc.mcp.server

import dev.chieppa.arc.plugins.secrets.MemorySecretStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class McpServerControlTest {
    private class FakeEndpoint(private val fails: Boolean) : McpEndpoint {
        override val clients = MutableStateFlow(0)
        var stopped = false

        override suspend fun start(port: Int): Int = if (fails) throw IOException("Address already in use") else port

        override fun stop() {
            stopped = true
        }
    }

    @Test
    fun itIsOffUntilSwitchedOnThenListensAndCountsClients() = runTest {
        val choices = mutableListOf<Boolean>()
        val made = mutableListOf<FakeEndpoint>()
        val control = McpServerControl(backgroundScope, McpToken(MemorySecretStore()), PORT, { FakeEndpoint(false).also(made::add) }, choices::add)
        assertEquals(McpServerState.Off, control.state.value)
        control.switch(true)
        runCurrent()
        assertEquals(McpServerState.Listening(PORT, 0), control.state.value)
        made.single().clients.value = 1
        runCurrent()
        assertEquals(McpServerState.Listening(PORT, 1), control.state.value)
        control.switch(false)
        assertEquals(McpServerState.Off, control.state.value)
        assertTrue(made.single().stopped)
        assertEquals(listOf(true, false), choices)
    }

    @Test
    fun aPortInUseShowsAsFailed() = runTest {
        val control = McpServerControl(backgroundScope, McpToken(MemorySecretStore()), PORT, { FakeEndpoint(true) })
        control.start()
        runCurrent()
        assertEquals(McpServerState.Failed("Address already in use"), control.state.value)
        assertTrue(control.on)
    }

    @Test
    fun anEndpointThatCannotBeMadeShowsAsFailed() = runTest {
        val control = McpServerControl(backgroundScope, McpToken(MemorySecretStore()), PORT, { throw IllegalStateException("No engine") })
        control.start()
        runCurrent()
        assertEquals(McpServerState.Failed("No engine"), control.state.value)
    }

    private companion object {
        const val PORT = 47830
    }
}

package dev.chieppa.arc.plugins.host

import dev.chieppa.arc.plugins.protocol.Contributions
import dev.chieppa.arc.plugins.protocol.Envelope
import dev.chieppa.arc.plugins.protocol.InitializeParams
import dev.chieppa.arc.plugins.protocol.PanelContribution
import dev.chieppa.arc.plugins.protocol.PluginInfo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class PluginSupervisorTest {
    private val timings = HostTimings(requestMillis = 100, initializeMillis = 100, heartbeatMillis = 1_000, restartMillis = 500, maxCrashes = 3, crashWindowMillis = 10_000)
    private val contributions = Contributions(panels = listOf(PanelContribution("todos", "TODOs")))
    private val ready = Preparation.Ready(LaunchSpec(listOf("node"), Path.of(".")), InitializeParams(1, PluginInfo("todos", "0.1.0", "."), "bundle.js"))
    private val calls = mutableListOf<String>()
    private val handler = object : RpcHandler {
        override suspend fun request(method: String, params: JsonElement): JsonElement {
            calls += method
            return JsonPrimitive("answer")
        }

        override fun notification(method: String, params: JsonElement) {
            calls += method
        }
    }

    private fun TestScope.supervisor(plugin: FakePlugin, prepare: Preparation = ready) =
        PluginSupervisor(backgroundScope, plugin, handler, PluginLog(), timings, { testScheduler.currentTime }) { prepare }

    @Test
    fun startsInitializesAndRunsWithItsContributions() = runTest {
        val plugin = FakePlugin(backgroundScope, contributions)
        val supervisor = supervisor(plugin).apply { start() }
        runCurrent()
        assertEquals(PluginStatus.Running(contributions), supervisor.status.value)
        plugin.current.write(Envelope.Request(1, "store.get"))
        runCurrent()
        assertEquals(listOf("store.get"), calls)
        assertEquals(Envelope.Response(1, JsonPrimitive("answer")), plugin.received.last())
    }

    @Test
    fun aCrashedProcessStartsAgainUntilItCrashesTooOften() = runTest {
        val plugin = FakePlugin(backgroundScope, contributions)
        val supervisor = supervisor(plugin).apply { start() }
        runCurrent()
        plugin.current.end(1)
        runCurrent()
        assertEquals(PluginStatus.Restarting(1), supervisor.status.value)
        advanceTimeBy(timings.restartMillis + 1)
        assertIs<PluginStatus.Running>(supervisor.status.value)
        assertEquals(2, plugin.launched.size)
        plugin.current.end(1)
        advanceTimeBy(timings.restartMillis + 1)
        plugin.current.log("TypeError: boom")
        runCurrent()
        plugin.current.end(1)
        runCurrent()
        val failed = assertIs<PluginStatus.Failed>(supervisor.status.value)
        assertEquals(DiagnosticKind.Crash, failed.diagnostics.single().kind)
        assertTrue("boom" in failed.diagnostics.single().message)
    }

    @Test
    fun aHungProcessIsEndedAndStartedAgain() = runTest {
        val plugin = FakePlugin(backgroundScope, contributions)
        val supervisor = supervisor(plugin).apply { start() }
        runCurrent()
        plugin.responsive = false
        advanceTimeBy(timings.heartbeatMillis + timings.requestMillis + 1)
        assertTrue(plugin.launched.first().killed)
        assertEquals(PluginStatus.Restarting(1), supervisor.status.value)
        plugin.responsive = true
        advanceTimeBy(timings.restartMillis + 1)
        assertIs<PluginStatus.Running>(supervisor.status.value)
    }

    @Test
    fun aLoadErrorOrAFailedPreparationLeavesItFailed() = runTest {
        val plugin = FakePlugin(backgroundScope).apply { loadError = "x is not defined @ src/index.tsx:3:7" }
        val loading = supervisor(plugin).apply { start() }
        runCurrent()
        assertEquals(
            PluginDiagnostic(DiagnosticKind.Load, "x is not defined", "src/index.tsx", 3, 7),
            assertIs<PluginStatus.Failed>(loading.status.value).diagnostics.single(),
        )
        val diagnostic = PluginDiagnostic(DiagnosticKind.Transpile, "Expected ;", "src/index.tsx", 14, 5)
        val transpiling = supervisor(FakePlugin(backgroundScope), Preparation.Failed(listOf(diagnostic))).apply { start() }
        runCurrent()
        assertEquals(PluginStatus.Failed(listOf(diagnostic)), transpiling.status.value)
    }

    @Test
    fun stoppingEndsTheProcess() = runTest {
        val plugin = FakePlugin(backgroundScope, contributions)
        val supervisor = supervisor(plugin).apply { start() }
        runCurrent()
        supervisor.stop()
        runCurrent()
        assertEquals(PluginStatus.Off, supervisor.status.value)
        assertTrue(plugin.current.killed)
    }
}

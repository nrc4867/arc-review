package dev.chieppa.arc.plugins.host

import dev.chieppa.arc.plugins.protocol.Envelope
import dev.chieppa.arc.plugins.protocol.LogParams
import dev.chieppa.arc.plugins.protocol.Methods
import dev.chieppa.arc.plugins.protocol.ProtocolJson
import dev.chieppa.arc.plugins.protocol.WorkerExitParams
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonNull
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class NodeHostTest {
    private val timings = HostTimings(requestMillis = 100, heartbeatMillis = 1_000)
    private val sources = Path.of("plugins").toAbsolutePath()
    private val other = Path.of("elsewhere").toAbsolutePath()

    private fun TestScope.host(node: FakeNodeProcess) =
        NodeHost(backgroundScope, node, { grants -> LaunchSpec(listOf("node") + grants.map { it.toString() }.sorted(), Path.of(".")) }, timings)

    private suspend fun NodeHost.start(plugin: String, root: Path = sources) = launch(WorkerSpec(plugin, root))

    @Test
    fun workersShareOneProcessThatRoutesByPluginAndStampsWhatArcSends() = runTest {
        val node = FakeNodeProcess(backgroundScope)
        val host = host(node)
        val todos = host.start("todos")
        val mermaid = host.start("mermaid")
        assertEquals(1, node.launched.size)
        assertEquals(listOf("todos", "mermaid"), node.started.map { it.plugin })
        todos.send(ProtocolJson.encode(Envelope.Request(1, Methods.PING)))
        runCurrent()
        assertEquals(Envelope.Request(1, Methods.PING, JsonNull, "todos"), node.forPlugins.single())
        node.current.write(Envelope.Notification(Methods.VIEW_PATCH, plugin = "mermaid"))
        node.current.write(Envelope.Notification(Methods.LOG, ProtocolJson.toElement(LogParams.serializer(), LogParams("hello")), "todos"))
        runCurrent()
        assertEquals(Methods.VIEW_PATCH, ProtocolJson.decode(mermaid.lines.receive())?.let { (it as Envelope.Notification).method })
        assertTrue(todos.lines.tryReceive().isFailure)
        assertEquals("hello", todos.logs.receive())
    }

    @Test
    fun aWorkersEndEndsOnlyThatWorker() = runTest {
        val node = FakeNodeProcess(backgroundScope)
        val host = host(node)
        val todos = host.start("todos")
        val mermaid = host.start("mermaid")
        val stale = WorkerExitParams("mermaid", node.started.last().generation + 1, 1)
        node.current.write(Envelope.Notification(Methods.HOST_WORKER_EXIT, ProtocolJson.toElement(WorkerExitParams.serializer(), stale)))
        val exit = WorkerExitParams("todos", node.started.first().generation, 3, "RangeError: out of memory")
        node.current.write(Envelope.Notification(Methods.HOST_WORKER_EXIT, ProtocolJson.toElement(WorkerExitParams.serializer(), exit)))
        runCurrent()
        assertEquals(3, todos.awaitExit())
        assertEquals("RangeError: out of memory", todos.logs.receive())
        val mermaidExit = async { mermaid.awaitExit() }
        runCurrent()
        assertTrue(mermaidExit.isActive)
        mermaidExit.cancel()
    }

    @Test
    fun theProcessEndingEndsEveryWorkerAndTheNextWorkerStartsItAgain() = runTest {
        val node = FakeNodeProcess(backgroundScope)
        val host = host(node)
        val todos = host.start("todos")
        val mermaid = host.start("mermaid")
        node.current.end(1)
        runCurrent()
        assertEquals(WorkerExits.HOST_ENDED, todos.awaitExit())
        assertEquals(WorkerExits.HOST_ENDED, mermaid.awaitExit())
        host.start("todos")
        assertEquals(2, node.launched.size)
    }

    @Test
    fun aFolderTheProcessCannotReadStartsItAgainWithThatFolderToo() = runTest {
        val node = FakeNodeProcess(backgroundScope)
        val host = host(node)
        val todos = host.start("todos")
        host.start("remote", other)
        runCurrent()
        assertEquals(WorkerExits.HOST_RESTARTED, todos.awaitExit())
        assertEquals(listOf("node", other.toString(), sources.toString()).sorted(), node.specs.last().command.sorted())
        assertTrue(node.launched.first().killed)
    }

    @Test
    fun aProcessThatStopsAnsweringIsEnded() = runTest {
        val node = FakeNodeProcess(backgroundScope)
        val todos = host(node).start("todos")
        node.responsive = false
        advanceTimeBy(timings.heartbeatMillis + timings.requestMillis + 1)
        assertTrue(node.current.killed)
        assertEquals(WorkerExits.HOST_ENDED, todos.awaitExit())
    }

    @Test
    fun killingAWorkerStopsItInTheProcess() = runTest {
        val node = FakeNodeProcess(backgroundScope)
        val todos = host(node).start("todos")
        todos.kill()
        runCurrent()
        assertEquals(listOf("todos"), node.stopped)
        assertEquals(WorkerExits.STOPPED, todos.awaitExit())
    }
}

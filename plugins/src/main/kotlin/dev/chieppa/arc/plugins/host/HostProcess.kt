package dev.chieppa.arc.plugins.host

import dev.chieppa.arc.plugins.protocol.Envelope
import dev.chieppa.arc.plugins.protocol.LogParams
import dev.chieppa.arc.plugins.protocol.Methods
import dev.chieppa.arc.plugins.protocol.PingParams
import dev.chieppa.arc.plugins.protocol.ProtocolJson
import dev.chieppa.arc.plugins.protocol.StartWorkerParams
import dev.chieppa.arc.plugins.protocol.StopWorkerParams
import dev.chieppa.arc.plugins.protocol.WorkerExitParams
import dev.chieppa.arc.plugins.protocol.WorkerLimits
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import java.io.IOException
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

/**
 * One run of the shared Node process on [connection], able to read [grants]: it hands each line to the worker it
 * names, answers for the process's main thread, pings the main thread every [HostTimings.heartbeatMillis] and ends a
 * process that misses an answer. When the process ends, every worker in it ends with [WorkerExits.HOST_ENDED]. What the
 * main thread prints goes to [log].
 */
internal class HostProcess(
    private val connection: LineConnection,
    val grants: Set<Path>,
    private val scope: CoroutineScope,
    private val timings: HostTimings,
    private val log: PluginLog,
) {
    private val workers = ConcurrentHashMap<String, Pair<Long, RoutedConnection>>()
    private val generations = AtomicLong()
    private val main = RoutedConnection(connection::send)
    private val peer = RpcPeer(main, scope, NO_CALLS, timings.requestMillis, log::add)
    private var endCode = WorkerExits.HOST_ENDED

    @Volatile
    var alive = true
        private set

    init {
        scope.launch {
            launch { for (line in connection.logs) log.add(line) }
            launch { for (line in connection.lines) route(line) }
            peer.start()
            val beat = launch { heartbeat() }
            connection.awaitExit()
            beat.cancel()
            ended()
        }
    }

    /** Starts [plugin]'s worker within [limits], ending one it had. Throws [IOException] when the process cannot. */
    suspend fun startWorker(plugin: String, limits: WorkerLimits): LineConnection {
        val generation = generations.incrementAndGet()
        lateinit var worker: RoutedConnection
        worker = RoutedConnection({ connection.send(ProtocolJson.stamp(it, plugin)) }) { stopWorker(plugin, worker) }
        workers.put(plugin, generation to worker)?.second?.end(WorkerExits.STOPPED)
        try {
            peer.request(Methods.HOST_START_WORKER, ProtocolJson.toElement(StartWorkerParams.serializer(), StartWorkerParams(plugin, generation, limits)))
        } catch (e: Exception) {
            workers.remove(plugin, generation to worker)
            worker.end(WorkerExits.HOST_ENDED)
            throw IOException(e.message ?: e.toString(), e)
        }
        return worker
    }

    /** Ends the process, its workers ending with [code]. */
    fun end(code: Int) {
        endCode = code
        connection.kill()
    }

    private fun stopWorker(plugin: String, worker: RoutedConnection) {
        val entry = workers[plugin] ?: return
        if (entry.second !== worker || !workers.remove(plugin, entry)) return
        scope.launch {
            runCatching { peer.request(Methods.HOST_STOP_WORKER, ProtocolJson.toElement(StopWorkerParams.serializer(), StopWorkerParams(plugin))) }
        }
    }

    private fun route(line: String) {
        val envelope = ProtocolJson.decode(line)
        val plugin = envelope?.plugin
        when {
            envelope == null -> log.add(line)
            plugin != null -> toWorker(plugin, envelope, line)
            envelope is Envelope.Notification && envelope.method == Methods.HOST_WORKER_EXIT -> workerEnded(envelope.params)
            else -> main.deliver(line)
        }
    }

    private fun toWorker(plugin: String, envelope: Envelope, line: String) {
        val worker = workers[plugin]?.second ?: return
        if (envelope is Envelope.Notification && envelope.method == Methods.LOG) {
            runCatching { ProtocolJson.fromElement(LogParams.serializer(), envelope.params) }.getOrNull()?.let { worker.log(it.text) }
        } else {
            worker.deliver(line)
        }
    }

    private fun workerEnded(params: JsonElement) {
        val exit = runCatching { ProtocolJson.fromElement(WorkerExitParams.serializer(), params) }.getOrNull() ?: return
        val entry = workers[exit.plugin]?.takeIf { it.first == exit.generation } ?: return
        if (!workers.remove(exit.plugin, entry)) return
        exit.error?.let(entry.second::log)
        entry.second.end(exit.code)
    }

    private suspend fun heartbeat() {
        var seq = 0L
        while (true) {
            delay(timings.heartbeatMillis)
            try {
                peer.request(Methods.PING, ProtocolJson.toElement(PingParams.serializer(), PingParams(++seq)))
            } catch (_: PluginTimeoutException) {
                log.add(HUNG)
                connection.kill()
                return
            } catch (_: PluginGoneException) {
                return
            }
        }
    }

    private fun ended() {
        alive = false
        main.end(endCode)
        workers.values.forEach { it.second.end(endCode) }
        workers.clear()
    }

    private companion object {
        const val HUNG = "The plugin host stopped answering"

        /** The main thread asks nothing of Arc. */
        val NO_CALLS = object : RpcHandler {
            override suspend fun request(method: String, params: JsonElement): JsonElement = JsonNull

            override fun notification(method: String, params: JsonElement) = Unit
        }
    }
}

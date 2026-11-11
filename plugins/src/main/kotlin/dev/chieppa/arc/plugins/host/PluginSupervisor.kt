package dev.chieppa.arc.plugins.host

import dev.chieppa.arc.plugins.protocol.ErrorCodes
import dev.chieppa.arc.plugins.protocol.InitializeParams
import dev.chieppa.arc.plugins.protocol.InitializeResult
import dev.chieppa.arc.plugins.protocol.Methods
import dev.chieppa.arc.plugins.protocol.PingParams
import dev.chieppa.arc.plugins.protocol.ProtocolJson
import dev.chieppa.arc.plugins.protocol.RpcException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import java.io.IOException

/**
 * Keeps one plugin's worker running: [prepare] makes it ready, [launcher] starts it, and it is sent `initialize`.
 * While it runs it is pinged every [HostTimings.heartbeatMillis]; one that misses an answer is hung and ended. A worker
 * that ends starts again after a pause, unless it has ended [HostTimings.maxCrashes] times within
 * [HostTimings.crashWindowMillis] by [clock], when it stays [PluginStatus.Failed] until started again; one ended only
 * because the shared process started again starts again at once. Its requests go to [handler], its log to [log];
 * [onRunning] runs once each start has initialized, as to mount its open views again.
 */
class PluginSupervisor(
    private val scope: CoroutineScope,
    private val launcher: WorkerLauncher,
    private val handler: RpcHandler,
    private val log: PluginLog,
    private val timings: HostTimings = HostTimings(),
    private val clock: () -> Long = System::currentTimeMillis,
    private val onRunning: suspend () -> Unit = {},
    private val prepare: suspend () -> Preparation,
) {
    private val state = MutableStateFlow<PluginStatus>(PluginStatus.Off)
    private var job: Job? = null

    @Volatile
    private var peer: RpcPeer? = null

    val status: StateFlow<PluginStatus> get() = state

    /** Starts the plugin, or starts it again from its files when it runs. */
    fun start() {
        job?.cancel()
        state.value = PluginStatus.Starting
        job = scope.launch { supervise() }
    }

    /** Ends the plugin's process and leaves it off. */
    fun stop() {
        job?.cancel()
        job = null
        peer = null
        state.value = PluginStatus.Off
    }

    /** Sends request [method] to the running process; throws [PluginGoneException] when none runs. */
    suspend fun request(method: String, params: JsonElement = JsonNull): JsonElement =
        (peer ?: throw PluginGoneException()).request(method, params)

    /** Sends notification [method] to the running process, if one runs. */
    suspend fun notify(method: String, params: JsonElement = JsonNull) {
        runCatching { peer?.notify(method, params) }
    }

    private suspend fun supervise() {
        val crashes = ArrayDeque<Long>()
        while (true) {
            state.value = PluginStatus.Starting
            val ready = when (val prepared = prepare()) {
                is Preparation.Failed -> {
                    state.value = PluginStatus.Failed(prepared.diagnostics)
                    return
                }
                is Preparation.Ready -> prepared
            }
            val ended = runOnce(ready) ?: continue
            val now = clock()
            crashes.addLast(now)
            while (crashes.first() < now - timings.crashWindowMillis) crashes.removeFirst()
            if (ended.kind != DiagnosticKind.Crash || crashes.size >= timings.maxCrashes) {
                state.value = PluginStatus.Failed(listOf(ended))
                return
            }
            state.value = PluginStatus.Restarting(crashes.size)
            delay(timings.restartMillis)
        }
    }

    /** Runs one worker until it ends, and says how it ended; null when it is to start again at once. */
    private suspend fun runOnce(ready: Preparation.Ready): PluginDiagnostic? {
        val connection = try {
            launcher.launch(ready.worker)
        } catch (e: IOException) {
            return PluginDiagnostic(DiagnosticKind.Load, e.message ?: e.toString())
        }
        try {
            return coroutineScope {
                launch { for (line in connection.logs) log.add(line) }
                val rpc = RpcPeer(connection, this, handler, timings.requestMillis, log::add)
                rpc.start()
                val ended = initialize(rpc, ready.initialize) ?: run {
                    peer = rpc
                    onRunning()
                    var hung = false
                    launch { hung = !heartbeat(rpc); if (hung) connection.kill() }
                    when (val code = connection.awaitExit()) {
                        WorkerExits.HOST_RESTARTED -> null
                        WorkerExits.HOST_ENDED -> PluginDiagnostic(DiagnosticKind.Crash, "$HOST_ENDED${lastLog()}")
                        else -> PluginDiagnostic(DiagnosticKind.Crash, if (hung) HUNG else "$EXITED $code${lastLog()}")
                    }
                }
                coroutineContext.cancelChildren()
                ended
            }
        } finally {
            peer = null
            connection.kill()
        }
    }

    /** Sends `initialize`; null once the plugin runs, else why it failed to load. */
    private suspend fun initialize(rpc: RpcPeer, params: InitializeParams): PluginDiagnostic? {
        val result = try {
            rpc.request(Methods.INITIALIZE, ProtocolJson.toElement(InitializeParams.serializer(), params), timings.initializeMillis)
        } catch (e: RpcException) {
            return LoadErrors.diagnostic(e.error.message, if (e.error.code == ErrorCodes.TRANSPILE) DiagnosticKind.Transpile else DiagnosticKind.Load)
        } catch (_: PluginTimeoutException) {
            return PluginDiagnostic(DiagnosticKind.Load, HUNG)
        } catch (_: PluginGoneException) {
            return PluginDiagnostic(DiagnosticKind.Crash, "$EXITED_EARLY${lastLog()}")
        }
        val contributions = runCatching { ProtocolJson.fromElement(InitializeResult.serializer(), result) }.getOrNull()?.contributions
            ?: return PluginDiagnostic(DiagnosticKind.Load, BAD_INITIALIZE)
        state.value = PluginStatus.Running(contributions)
        return null
    }

    /** Pings until a ping goes unanswered, giving false, or the process ends, giving true. */
    private suspend fun heartbeat(rpc: RpcPeer): Boolean {
        var seq = 0L
        while (true) {
            delay(timings.heartbeatMillis)
            try {
                rpc.request(Methods.PING, ProtocolJson.toElement(PingParams.serializer(), PingParams(++seq)))
            } catch (_: PluginTimeoutException) {
                return false
            } catch (_: PluginGoneException) {
                return true
            }
        }
    }

    private fun lastLog(): String = log.lines.value.lastOrNull()?.let { ": $it" }.orEmpty()

    private companion object {
        const val HUNG = "Stopped answering"
        const val EXITED = "Exited with code"
        const val EXITED_EARLY = "Exited while loading"
        const val HOST_ENDED = "The plugin host ended"
        const val BAD_INITIALIZE = "Answered initialize with something else"
    }
}

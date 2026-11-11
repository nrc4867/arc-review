package dev.chieppa.arc.ui.plugins

import dev.chieppa.arc.plugins.host.LineConnection
import dev.chieppa.arc.plugins.host.WorkerLauncher
import dev.chieppa.arc.plugins.host.WorkerSpec
import dev.chieppa.arc.plugins.protocol.Contributions
import dev.chieppa.arc.plugins.protocol.Envelope
import dev.chieppa.arc.plugins.protocol.InitializeResult
import dev.chieppa.arc.plugins.protocol.Methods
import dev.chieppa.arc.plugins.protocol.MountParams
import dev.chieppa.arc.plugins.protocol.ProtocolJson
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull

/**
 * A plugin's worker played by a UI test: each start answers `initialize` with what [contributions] holds then and draws
 * each mounted view as [draw] gives its patch ops (JSON) for the mount. Everything Arc sent is kept in [received].
 */
class FakePluginProcess(var contributions: Contributions, private val draw: (MountParams) -> String) : WorkerLauncher {
    val received = mutableListOf<Envelope>()

    /** How many times the worker started. */
    var starts = 0
        private set

    override suspend fun launch(spec: WorkerSpec): LineConnection {
        starts++
        return Worker()
    }

    private inner class Worker : LineConnection {
        private val out = Channel<String>(Channel.UNLIMITED)
        private val err = Channel<String>(Channel.UNLIMITED)
        private val exit = CompletableDeferred<Int>()

        override val lines: ReceiveChannel<String> get() = out
        override val logs: ReceiveChannel<String> get() = err

        override suspend fun send(line: String) {
            val envelope = ProtocolJson.decode(line) ?: return
            received += envelope
            if (envelope !is Envelope.Request) return
            when (envelope.method) {
                Methods.INITIALIZE -> out.trySend(ProtocolJson.encode(Envelope.Response(envelope.id, ProtocolJson.toElement(InitializeResult.serializer(), InitializeResult(contributions)))))
                Methods.VIEW_MOUNT -> {
                    val mount = ProtocolJson.fromElement(MountParams.serializer(), envelope.params)
                    out.trySend(ProtocolJson.encode(Envelope.Response(envelope.id, JsonNull)))
                    val patch = """{"view":"${mount.view}","ops":${draw(mount)}}"""
                    out.trySend(ProtocolJson.encode(Envelope.Notification(Methods.VIEW_PATCH, Json.parseToJsonElement(patch))))
                }
                else -> out.trySend(ProtocolJson.encode(Envelope.Response(envelope.id, JsonNull)))
            }
        }

        override suspend fun awaitExit(): Int = exit.await()

        override fun kill() {
            exit.complete(0)
            out.close()
        }
    }
}

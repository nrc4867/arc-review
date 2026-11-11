package dev.chieppa.arc.ui.plugins

import dev.chieppa.arc.plugins.host.LaunchSpec
import dev.chieppa.arc.plugins.host.PluginConnection
import dev.chieppa.arc.plugins.host.PluginLauncher
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
 * A plugin played by a UI test: it answers `initialize` with [contributions] and draws each mounted view as [draw]
 * gives its patch ops (JSON) for the mount. Everything Arc sent is kept in [received].
 */
class FakePluginProcess(private val contributions: Contributions, private val draw: (MountParams) -> String) : PluginLauncher, PluginConnection {
    private val out = Channel<String>(Channel.UNLIMITED)
    private val err = Channel<String>(Channel.UNLIMITED)
    private val exit = CompletableDeferred<Int>()
    val received = mutableListOf<Envelope>()

    override val lines: ReceiveChannel<String> get() = out
    override val logs: ReceiveChannel<String> get() = err

    override fun launch(spec: LaunchSpec): PluginConnection = this

    override suspend fun send(line: String) {
        val envelope = ProtocolJson.decode(line) ?: return
        received += envelope
        if (envelope !is Envelope.Request) return
        when (envelope.method) {
            Methods.INITIALIZE -> reply(envelope, ProtocolJson.encode(Envelope.Response(envelope.id, ProtocolJson.toElement(InitializeResult.serializer(), InitializeResult(contributions)))))
            Methods.VIEW_MOUNT -> {
                val mount = ProtocolJson.fromElement(MountParams.serializer(), envelope.params)
                reply(envelope, ProtocolJson.encode(Envelope.Response(envelope.id, JsonNull)))
                val patch = """{"view":"${mount.view}","ops":${draw(mount)}}"""
                out.trySend(ProtocolJson.encode(Envelope.Notification(Methods.VIEW_PATCH, Json.parseToJsonElement(patch))))
            }
            else -> reply(envelope, ProtocolJson.encode(Envelope.Response(envelope.id, JsonNull)))
        }
    }

    @Suppress("UNUSED_PARAMETER")
    private fun reply(request: Envelope.Request, line: String) {
        out.trySend(line)
    }

    override suspend fun awaitExit(): Int = exit.await()

    override fun kill() {
        exit.complete(0)
    }
}

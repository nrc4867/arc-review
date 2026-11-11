package dev.chieppa.arc.plugins.host

import dev.chieppa.arc.plugins.protocol.Envelope
import dev.chieppa.arc.plugins.protocol.Methods
import dev.chieppa.arc.plugins.protocol.ProtocolJson
import dev.chieppa.arc.plugins.protocol.StartWorkerParams
import dev.chieppa.arc.plugins.protocol.StopWorkerParams
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonNull

/**
 * The plugins' Node process played by a test: its main thread answers pings while [responsive] and starts and stops
 * workers, keeping what it was asked in [started] and [stopped]; messages for plugins land in [forPlugins]. Each launch
 * is kept in [launched] with its spec in [specs].
 */
class FakeNodeProcess(private val scope: CoroutineScope) : ProcessLauncher {
    val launched = mutableListOf<FakeConnection>()
    val specs = mutableListOf<LaunchSpec>()
    val started = mutableListOf<StartWorkerParams>()
    val stopped = mutableListOf<String>()
    val forPlugins = mutableListOf<Envelope>()
    var responsive = true

    val current: FakeConnection get() = launched.last()

    override fun launch(spec: LaunchSpec): LineConnection {
        val connection = FakeConnection()
        launched += connection
        specs += spec
        scope.launch {
            for (envelope in connection.sent) {
                if (envelope.plugin != null) forPlugins += envelope
                else if (envelope is Envelope.Request) answer(connection, envelope)
            }
        }
        return connection
    }

    private fun answer(connection: FakeConnection, request: Envelope.Request) {
        when (request.method) {
            Methods.PING -> if (!responsive) return
            Methods.HOST_START_WORKER -> started += ProtocolJson.fromElement(StartWorkerParams.serializer(), request.params)
            Methods.HOST_STOP_WORKER -> stopped += ProtocolJson.fromElement(StopWorkerParams.serializer(), request.params).plugin
        }
        connection.write(Envelope.Response(request.id, JsonNull))
    }
}

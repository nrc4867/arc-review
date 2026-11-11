package dev.chieppa.arc.plugins.host

import dev.chieppa.arc.plugins.protocol.Contributions
import dev.chieppa.arc.plugins.protocol.Envelope
import dev.chieppa.arc.plugins.protocol.InitializeResult
import dev.chieppa.arc.plugins.protocol.Methods
import dev.chieppa.arc.plugins.protocol.ProtocolJson
import dev.chieppa.arc.plugins.protocol.RpcError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull

/**
 * Launches [FakeConnection]s that answer like a plugin's worker: `initialize` with [contributions] (or [loadError]),
 * pings while [responsive], and every other request with what [answers] gives for its method, else null. Each launch is
 * kept in [launched], with the worker asked for in [workers], and everything Arc sent in [received]. As a
 * [ProcessLauncher] it plays a program the same way.
 */
class FakePlugin(private val scope: CoroutineScope, private val contributions: Contributions = Contributions()) : WorkerLauncher, ProcessLauncher {
    val launched = mutableListOf<FakeConnection>()
    val received = mutableListOf<Envelope>()
    val workers = mutableListOf<WorkerSpec>()
    val answers = mutableMapOf<String, JsonElement>()
    var responsive = true
    var loadError: String? = null
    var loadErrorCode: String = "failed"

    val current: FakeConnection get() = launched.last()

    override suspend fun launch(spec: WorkerSpec): LineConnection {
        workers += spec
        return start()
    }

    override fun launch(spec: LaunchSpec): LineConnection = start()

    private fun start(): LineConnection {
        val connection = FakeConnection()
        launched += connection
        scope.launch {
            for (envelope in connection.sent) {
                received += envelope
                if (envelope is Envelope.Request) answer(connection, envelope)
            }
        }
        return connection
    }

    private fun answer(connection: FakeConnection, request: Envelope.Request) {
        val error = loadError
        when {
            request.method == Methods.PING && !responsive -> Unit
            request.method == Methods.INITIALIZE && error != null ->
                connection.write(Envelope.Response(request.id, error = RpcError(loadErrorCode, error)))
            request.method == Methods.INITIALIZE ->
                connection.write(Envelope.Response(request.id, ProtocolJson.toElement(InitializeResult.serializer(), InitializeResult(contributions))))
            else -> connection.write(Envelope.Response(request.id, answers[request.method] ?: JsonNull))
        }
    }
}

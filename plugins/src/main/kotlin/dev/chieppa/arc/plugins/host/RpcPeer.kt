package dev.chieppa.arc.plugins.host

import dev.chieppa.arc.plugins.protocol.Envelope
import dev.chieppa.arc.plugins.protocol.ErrorCodes
import dev.chieppa.arc.plugins.protocol.ProtocolJson
import dev.chieppa.arc.plugins.protocol.RpcError
import dev.chieppa.arc.plugins.protocol.RpcException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import kotlin.coroutines.cancellation.CancellationException

/**
 * Arc's end of the protocol on one [connection]: it numbers requests and waits for their answers at most
 * [timeoutMillis], answers the plugin's requests through [handler] and passes on its notifications. A line on standard
 * output that is not an envelope goes to [onStray], as for a plugin that printed there. Runs on [scope].
 */
class RpcPeer(
    private val connection: PluginConnection,
    private val scope: CoroutineScope,
    private val handler: RpcHandler,
    private val timeoutMillis: Long,
    private val onStray: (String) -> Unit = {},
) {
    private val ids = AtomicLong()
    private val pending = ConcurrentHashMap<Long, CompletableDeferred<Envelope.Response>>()

    /** Reads the connection's lines until it closes, failing every request still waiting then. */
    fun start(): Job = scope.launch {
        try {
            for (line in connection.lines) receive(line)
        } finally {
            val waiting = pending.values.toList()
            pending.clear()
            waiting.forEach { it.completeExceptionally(PluginGoneException()) }
        }
    }

    /**
     * Sends request [method] and gives its result. Throws [RpcException] when the plugin refuses it,
     * [PluginTimeoutException] when it does not answer in time and [PluginGoneException] when its process ends first.
     */
    suspend fun request(method: String, params: JsonElement = JsonNull, timeout: Long = timeoutMillis): JsonElement {
        val id = ids.incrementAndGet()
        val answer = CompletableDeferred<Envelope.Response>()
        pending[id] = answer
        try {
            connection.send(ProtocolJson.encode(Envelope.Request(id, method, params)))
            val response = withTimeout(timeout) { answer.await() }
            response.error?.let { throw RpcException(it) }
            return response.result
        } catch (_: TimeoutCancellationException) {
            throw PluginTimeoutException(method)
        } finally {
            pending.remove(id)
        }
    }

    /** Sends notification [method]. */
    suspend fun notify(method: String, params: JsonElement = JsonNull) {
        connection.send(ProtocolJson.encode(Envelope.Notification(method, params)))
    }

    private fun receive(line: String) {
        when (val envelope = ProtocolJson.decode(line)) {
            null -> onStray(line)
            is Envelope.Response -> pending[envelope.id]?.complete(envelope)
            is Envelope.Notification -> handler.notification(envelope.method, envelope.params)
            is Envelope.Request -> scope.launch { answer(envelope) }
        }
    }

    private suspend fun answer(request: Envelope.Request) {
        val response = try {
            Envelope.Response(request.id, handler.request(request.method, request.params))
        } catch (e: RpcException) {
            Envelope.Response(request.id, error = e.error)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Envelope.Response(request.id, error = RpcError(ErrorCodes.FAILED, e.message ?: e.toString()))
        }
        runCatching { connection.send(ProtocolJson.encode(response)) }
    }
}

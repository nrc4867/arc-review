package dev.chieppa.arc.mcp.server

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Switches Arc's MCP server on and off on [scope]: on, it starts the endpoint [endpoint] makes with the [token] on
 * [port] and follows its sessions; off, it stops it. [onChange] keeps the person's choice. The server is off until the
 * person switches it on. A token or endpoint that cannot be had, or a port that cannot be bound, leaves it [McpServerState.Failed]
 * with the reason.
 */
class McpServerControl(
    private val scope: CoroutineScope,
    val token: McpToken,
    val port: Int,
    private val endpoint: (token: String) -> McpEndpoint,
    private val onChange: (Boolean) -> Unit = {},
) {
    private val shown = MutableStateFlow<McpServerState>(McpServerState.Off)
    private var running: McpEndpoint? = null
    private var job: Job? = null

    val state: StateFlow<McpServerState> get() = shown

    /** Whether the person has switched it on. */
    val on: Boolean get() = shown.value != McpServerState.Off

    /** Switches it [on] or off, keeping the choice. */
    fun switch(on: Boolean) {
        onChange(on)
        if (on) start() else stop()
    }

    /** Starts it, as when Arc opens with it switched on. */
    @Synchronized
    fun start() {
        if (job != null) return
        shown.value = McpServerState.Starting
        job = scope.launch {
            val server = failing { endpoint(token.current()) } ?: return@launch
            running = server
            val listening = failing { server.start(port) } ?: return@launch
            server.clients.collect { shown.value = McpServerState.Listening(listening, it) }
        }
    }

    /** What [call] gives, or null with the server shown as failed for the reason it threw. */
    private inline fun <T : Any> failing(call: () -> T): T? = try {
        call()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        shown.value = McpServerState.Failed(e.message ?: e.toString())
        null
    }

    /** Stops it, leaving it off. */
    @Synchronized
    fun stop() {
        job?.cancel()
        job = null
        running?.stop()
        running = null
        shown.value = McpServerState.Off
    }
}

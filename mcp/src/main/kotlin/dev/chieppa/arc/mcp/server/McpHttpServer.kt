package dev.chieppa.arc.mcp.server

import dev.chieppa.arc.mcp.tools.ArcTool
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCallPipeline
import io.ktor.server.application.call
import io.ktor.server.cio.CIO
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.request.header
import io.ktor.server.response.respond
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.server.mcpStreamableHttp
import io.modelcontextprotocol.kotlin.sdk.types.Implementation
import io.modelcontextprotocol.kotlin.sdk.types.ServerCapabilities
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Duration.Companion.minutes

/**
 * Arc's MCP server: the protocol through the MCP SDK's streamable HTTP transport, served by Ktor's CIO engine on
 * 127.0.0.1 only, with [tools] and nothing else. Every request passes [RequestGuard] first, holding [token]. [clients]
 * counts the client sessions open now.
 */
class McpHttpServer(private val tools: List<ArcTool>, private val token: String, private val version: String) : McpEndpoint {
    private val listening = AtomicInteger()
    private val open = MutableStateFlow(0)
    private val sessions = mutableSetOf<String>()
    private val guard = RequestGuard(token) { listening.get() }
    private var engine: EmbeddedServer<*, *>? = null

    /** How many client sessions are open. */
    override val clients: StateFlow<Int> get() = open

    /** Starts listening on [port] (0 for any free port) and gives the port it listens on; throws when it cannot. */
    override suspend fun start(port: Int): Int {
        val server = embeddedServer(CIO, host = McpConstants.HOST, port = port) { configure(protocol()) }
        server.startSuspend(wait = false)
        engine = server
        return server.engine.resolvedConnectors().first().port.also(listening::set)
    }

    override fun stop() {
        engine?.stop(McpConstants.STOP_GRACE_MILLIS, McpConstants.STOP_TIMEOUT_MILLIS)
        engine = null
        synchronized(sessions) { sessions.clear() }
        open.value = 0
    }

    private fun Application.configure(server: Server) {
        intercept(ApplicationCallPipeline.Plugins) {
            val verdict = guard.check(call.request.header(HttpHeaders.Host), call.request.header(HttpHeaders.Origin), call.request.header(HttpHeaders.Authorization))
            if (verdict != GuardVerdict.Allowed) {
                call.respond(HttpStatusCode.fromValue(verdict.status))
                finish()
            }
        }
        mcpStreamableHttp(
            path = McpConstants.PATH,
            allowedHosts = listOf(McpConstants.HOST, McpConstants.LOCALHOST),
            sessionIdleTimeout = McpConstants.SESSION_IDLE_MINUTES.minutes,
        ) { server }
    }

    /** One protocol server for every session, so each session's tools are the same. */
    private fun protocol(): Server {
        val server = Server(
            Implementation(McpConstants.NAME, version),
            ServerOptions(ServerCapabilities(tools = ServerCapabilities.Tools(listChanged = false))),
            McpConstants.INSTRUCTIONS,
        )
        tools.forEach { tool -> server.addTool(ToolCalls.describe(tool)) { request -> ToolCalls.call(tool, request.arguments) } }
        server.onConnect { follow(server) }
        return server
    }

    /** Counts the sessions open, following each new one until it closes. */
    private fun follow(server: Server) {
        server.sessions.values.forEach { session ->
            val added = synchronized(sessions) { sessions.add(session.sessionId) }
            if (added) {
                session.onClose {
                    synchronized(sessions) { sessions.remove(session.sessionId) }
                    open.value = synchronized(sessions) { sessions.size }
                }
            }
        }
        open.value = synchronized(sessions) { sessions.size }
    }
}

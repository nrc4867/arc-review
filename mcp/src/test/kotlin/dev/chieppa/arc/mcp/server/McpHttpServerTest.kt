package dev.chieppa.arc.mcp.server

import dev.chieppa.arc.mcp.tools.ArcTool
import dev.chieppa.arc.mcp.tools.ToolAnswer
import dev.chieppa.arc.mcp.tools.ToolArgs
import dev.chieppa.arc.mcp.tools.ToolInput
import dev.chieppa.arc.mcp.tools.ToolPart
import dev.chieppa.arc.mcp.tools.ToolRefusal
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.net.Socket
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** The server over real HTTP on 127.0.0.1 and a port the system picks: the protocol, the token, Origin and Host. */
class McpHttpServerTest {
    private val echo = object : ArcTool {
        override val name = "echo"
        override val description = "Says what it is given."
        override val input = ToolInput.of { string("text", "What to say.", required = true) }
        override val readOnly = true

        override suspend fun call(args: JsonObject): ToolAnswer {
            val text = ToolArgs.requiredString(args, "text")
            if (text == "no") throw ToolRefusal("Will not say no")
            return ToolAnswer(listOf(ToolPart.Text(text), ToolPart.Png(byteArrayOf(1, 2, 3))))
        }
    }
    private val server = McpHttpServer(listOf(echo), TOKEN, "0.1.0")
    private val port = runBlocking { server.start(0) }
    private val client = HttpClient.newHttpClient()

    @AfterTest
    fun stop() {
        server.stop()
    }

    private fun post(body: String, session: String? = null, token: String? = TOKEN, origin: String? = null): HttpResponse<String> {
        val request = HttpRequest.newBuilder(URI.create(ConnectCommand.url(port)))
            .header("Content-Type", "application/json")
            .header("Accept", "application/json, text/event-stream")
            .apply {
                token?.let { header("Authorization", "Bearer $it") }
                origin?.let { header("Origin", it) }
                session?.let { header("Mcp-Session-Id", it) }
            }
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build()
        return client.send(request, HttpResponse.BodyHandlers.ofString())
    }

    private fun initialize(): String {
        val response = post(INITIALIZE)
        assertEquals(200, response.statusCode(), response.body())
        val session = response.headers().firstValue("Mcp-Session-Id").get()
        post("""{"jsonrpc":"2.0","method":"notifications/initialized"}""", session)
        return session
    }

    @Test
    fun aClientWithTheTokenListsAndCallsTheTools() = runBlocking<Unit> {
        val session = initialize()
        server.clients.first { it == 1 }
        val tools = Json.parseToJsonElement(post("""{"jsonrpc":"2.0","id":2,"method":"tools/list"}""", session).body()).jsonObject
        val tool = tools.getValue("result").jsonObject.getValue("tools").jsonArray.single().jsonObject
        assertEquals("echo", tool.getValue("name").jsonPrimitive.content)
        assertEquals(listOf("text"), tool.getValue("inputSchema").jsonObject.getValue("required").jsonArray.map { it.jsonPrimitive.content })

        val call = post("""{"jsonrpc":"2.0","id":3,"method":"tools/call","params":{"name":"echo","arguments":{"text":"hello"}}}""", session)
        val content = Json.parseToJsonElement(call.body()).jsonObject.getValue("result").jsonObject.getValue("content").jsonArray
        assertEquals("hello", content[0].jsonObject.getValue("text").jsonPrimitive.content)
        assertEquals("image/png", content[1].jsonObject.getValue("mimeType").jsonPrimitive.content)
        assertEquals("AQID", content[1].jsonObject.getValue("data").jsonPrimitive.content)

        val refused = post("""{"jsonrpc":"2.0","id":4,"method":"tools/call","params":{"name":"echo","arguments":{"text":"no"}}}""", session)
        val result = Json.parseToJsonElement(refused.body()).jsonObject.getValue("result").jsonObject
        assertEquals("true", result.getValue("isError").jsonPrimitive.content)
        assertTrue("Will not say no" in result.toString())
    }

    @Test
    fun noTokenOrAWrongOneIsUnauthorized() {
        assertEquals(401, post(INITIALIZE, token = null).statusCode())
        assertEquals(401, post(INITIALIZE, token = "guess").statusCode())
        assertEquals(0, server.clients.value)
    }

    @Test
    fun aRequestWithAnOriginIsRefused() {
        assertEquals(403, post(INITIALIZE, origin = "http://localhost:$port").statusCode())
    }

    @Test
    fun aRequestNamingAnotherHostIsRefused() {
        Socket(McpConstants.HOST, port).use { socket ->
            val request = "POST /mcp HTTP/1.1\r\nHost: rebound.example:$port\r\nAuthorization: Bearer $TOKEN\r\n" +
                "Content-Type: application/json\r\nContent-Length: ${INITIALIZE.length}\r\nConnection: close\r\n\r\n$INITIALIZE"
            socket.getOutputStream().write(request.toByteArray())
            val status = socket.getInputStream().bufferedReader().readLine()
            assertTrue(" 403 " in status, status)
        }
    }

    private companion object {
        const val TOKEN = "test-token"
        const val INITIALIZE = """{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2025-06-18","capabilities":{},"clientInfo":{"name":"test","version":"1"}}}"""
    }
}

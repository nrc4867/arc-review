package dev.chieppa.arc.mcp.server

import kotlin.test.Test
import kotlin.test.assertEquals

class RequestGuardTest {
    private val guard = RequestGuard(TOKEN) { PORT }

    @Test
    fun aLocalClientWithTheTokenIsLetThrough() {
        assertEquals(GuardVerdict.Allowed, guard.check("127.0.0.1:$PORT", null, "Bearer $TOKEN"))
        assertEquals(GuardVerdict.Allowed, guard.check("LOCALHOST:$PORT", null, "bearer $TOKEN"))
    }

    @Test
    fun noTokenOrAnotherIsUnauthorized() {
        assertEquals(GuardVerdict.Unauthorized, guard.check("127.0.0.1:$PORT", null, null))
        assertEquals(GuardVerdict.Unauthorized, guard.check("127.0.0.1:$PORT", null, "Bearer ${TOKEN}x"))
        assertEquals(GuardVerdict.Unauthorized, guard.check("127.0.0.1:$PORT", null, TOKEN))
        assertEquals(GuardVerdict.Unauthorized, guard.check("127.0.0.1:$PORT", null, "Basic $TOKEN"))
    }

    @Test
    fun aWebPageIsRefusedEvenWithTheToken() {
        assertEquals(GuardVerdict.BrowserOrigin, guard.check("127.0.0.1:$PORT", "http://localhost:3000", "Bearer $TOKEN"))
        assertEquals(GuardVerdict.BrowserOrigin, guard.check("127.0.0.1:$PORT", "null", "Bearer $TOKEN"))
    }

    @Test
    fun aRequestForAnotherHostOrPortIsRefused() {
        assertEquals(GuardVerdict.WrongHost, guard.check("evil.example:$PORT", null, "Bearer $TOKEN"))
        assertEquals(GuardVerdict.WrongHost, guard.check("127.0.0.1:1", null, "Bearer $TOKEN"))
        assertEquals(GuardVerdict.WrongHost, guard.check(null, null, "Bearer $TOKEN"))
    }

    @Test
    fun theCommandNamesTheLoopbackEndpointAndTheToken() {
        assertEquals(
            "claude mcp add --transport http arc http://127.0.0.1:$PORT/mcp --header \"Authorization: Bearer $TOKEN\"",
            ConnectCommand.of(PORT, TOKEN),
        )
    }

    private companion object {
        const val TOKEN = "s3cret-token"
        const val PORT = 47830
    }
}

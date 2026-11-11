package dev.chieppa.arc.plugins.protocol

import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNull

class ProtocolJsonTest {
    @Test
    fun envelopesRoundTripOnOneLine() {
        val params = ProtocolJson.toElement(ActionRunParams.serializer(), ActionRunParams("run-tests", CallContext(file = FileInfo("a.ts", 3))))
        val envelopes = listOf(
            Envelope.Request(7, Methods.ACTION_RUN, params),
            Envelope.Response(7, JsonPrimitive("done")),
            Envelope.Response(8, error = RpcError(ErrorCodes.DENIED, "no console")),
            Envelope.Notification(Methods.SETTINGS_CHANGED, buildJsonObject { put("values", buildJsonObject { put("a", 1) }) }),
        )
        for (envelope in envelopes) {
            val line = ProtocolJson.encode(envelope)
            assertEquals(false, '\n' in line)
            assertEquals(envelope, ProtocolJson.decode(line))
        }
    }

    @Test
    fun readsWhatTheJavaScriptSideWrites() {
        val line = """{"kind":"notification","method":"view.patch","params":{"view":"v1","ops":[""" +
            """{"op":"create","id":1,"type":"Button","props":{"label":"Go","onClick":{"${'$'}handler":true}}},""" +
            """{"op":"append","parent":0,"child":1}],"extra":1}}"""
        val envelope = assertIs<Envelope.Notification>(ProtocolJson.decode(line))
        val patch = ProtocolJson.fromElement(PatchParams.serializer(), envelope.params)
        assertEquals("v1", patch.view)
        assertIs<PatchOp.Create>(patch.ops[0])
        assertEquals(PatchOp.Append(0, 1), patch.ops[1])
    }

    @Test
    fun aLineThatIsNotAnEnvelopeIsNull() {
        assertNull(ProtocolJson.decode("hello from console.log"))
        assertNull(ProtocolJson.decode("""{"kind":"other"}"""))
    }

    @Test
    fun badParamsAreAnRpcError() {
        val failure = assertFailsWith<RpcException> { ProtocolJson.fromElement(PingParams.serializer(), JsonPrimitive(1)) }
        assertEquals(ErrorCodes.BAD_PARAMS, failure.error.code)
    }
}

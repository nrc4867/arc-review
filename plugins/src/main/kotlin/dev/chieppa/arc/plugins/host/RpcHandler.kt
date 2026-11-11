package dev.chieppa.arc.plugins.host

import kotlinx.serialization.json.JsonElement

/** What a peer does with the requests and notifications the other side sends. */
interface RpcHandler {
    /** Answers request [method]; throws [dev.chieppa.arc.plugins.protocol.RpcException] to refuse it. */
    suspend fun request(method: String, params: JsonElement): JsonElement

    fun notification(method: String, params: JsonElement)
}

package dev.chieppa.arc.plugins.protocol

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull

/**
 * One message between Arc and the plugins' Node process, one JSON object per line over the process's standard input
 * and output. Both sides send all three kinds: a [Request] waits for the [Response] with its id, a [Notification] does
 * not. A message to or from a plugin names it in [plugin]: Arc names the plugin a message is for, and the process's
 * main thread stamps every message a plugin's worker sends with that plugin's id, whatever the worker wrote. A message
 * without one is between Arc and the process itself. The method names are in [Methods] and each method's params and
 * result are the `@Serializable` types beside this.
 */
@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("kind")
sealed interface Envelope {
    val plugin: String?

    @Serializable
    @SerialName("request")
    data class Request(val id: Long, val method: String, val params: JsonElement = JsonNull, override val plugin: String? = null) : Envelope

    @Serializable
    @SerialName("response")
    data class Response(val id: Long, val result: JsonElement = JsonNull, val error: RpcError? = null, override val plugin: String? = null) : Envelope

    @Serializable
    @SerialName("notification")
    data class Notification(val method: String, val params: JsonElement = JsonNull, override val plugin: String? = null) : Envelope
}

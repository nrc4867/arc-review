package dev.chieppa.arc.plugins.protocol

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject

/** The JSON both sides write: unknown fields ignored, so a newer peer's additions do no harm. */
object ProtocolJson {
    val json: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = false
    }

    /** [envelope] as one line. */
    fun encode(envelope: Envelope): String = json.encodeToString(Envelope.serializer(), envelope)

    /** The envelope [line] holds, or null when it is not one. */
    fun decode(line: String): Envelope? = try {
        json.decodeFromString(Envelope.serializer(), line)
    } catch (_: SerializationException) {
        null
    } catch (_: IllegalArgumentException) {
        null
    }

    /** [line], an envelope, addressed to [plugin]. */
    fun stamp(line: String, plugin: String): String {
        val message = json.parseToJsonElement(line).jsonObject
        return json.encodeToString(JsonObject.serializer(), JsonObject(message + (PLUGIN to JsonPrimitive(plugin))))
    }

    private const val PLUGIN = "plugin"

    fun <T> toElement(serializer: KSerializer<T>, value: T): JsonElement = json.encodeToJsonElement(serializer, value)

    /** [element] read as [serializer]'s type; throws [RpcException] with [ErrorCodes.BAD_PARAMS] when it is not one. */
    fun <T> fromElement(serializer: KSerializer<T>, element: JsonElement): T = try {
        json.decodeFromJsonElement(serializer, element)
    } catch (e: SerializationException) {
        throw RpcException(RpcError(ErrorCodes.BAD_PARAMS, e.message ?: e.toString()))
    } catch (e: IllegalArgumentException) {
        throw RpcException(RpcError(ErrorCodes.BAD_PARAMS, e.message ?: e.toString()))
    }
}

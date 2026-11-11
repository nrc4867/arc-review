package dev.chieppa.arc.plugins.view

import dev.chieppa.arc.plugins.protocol.PatchOp
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive

/** Reading a node's props by name, forgiving of a plugin that passes the wrong kind of value. */
object Props {
    fun string(props: JsonObject, name: String): String? = (props[name] as? JsonPrimitive)?.takeIf { it.isString }?.content

    fun boolean(props: JsonObject, name: String): Boolean? = (props[name] as? JsonPrimitive)?.booleanOrNull

    fun int(props: JsonObject, name: String): Int? = (props[name] as? JsonPrimitive)?.intOrNull

    fun number(props: JsonObject, name: String): Double? = (props[name] as? JsonPrimitive)?.doubleOrNull

    fun array(props: JsonObject, name: String): List<JsonElement> = (props[name] as? JsonArray).orEmpty()

    fun strings(props: JsonObject, name: String): List<String> =
        array(props, name).mapNotNull { (it as? JsonPrimitive)?.takeIf { p -> p.isString }?.content }

    /** Whether the plugin passed a function as [name], which a view event calls. */
    fun handler(props: JsonObject, name: String): Boolean =
        (props[name] as? JsonObject)?.get(PatchOp.HANDLER)?.jsonPrimitive?.booleanOrNull == true
}

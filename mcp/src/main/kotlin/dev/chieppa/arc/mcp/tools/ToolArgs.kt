package dev.chieppa.arc.mcp.tools

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.intOrNull

/** Reads a tool call's arguments, refusing one of the wrong kind or a required one missing. */
object ToolArgs {
    private const val MISSING = "Missing argument"
    private const val NOT_TEXT = "Expected text for"
    private const val NOT_NUMBER = "Expected a whole number for"
    private const val NOT_BOOLEAN = "Expected true or false for"
    private const val NOT_OBJECT = "Expected an object for"

    fun string(args: JsonObject, name: String): String? {
        val value = args[name] ?: return null
        return (value as? JsonPrimitive)?.takeIf { it.isString }?.content ?: throw ToolRefusal("$NOT_TEXT $name")
    }

    fun requiredString(args: JsonObject, name: String): String = string(args, name) ?: throw ToolRefusal("$MISSING $name")

    fun int(args: JsonObject, name: String): Int? {
        val value = args[name] ?: return null
        return (value as? JsonPrimitive)?.takeIf { !it.isString }?.intOrNull ?: throw ToolRefusal("$NOT_NUMBER $name")
    }

    fun boolean(args: JsonObject, name: String): Boolean? {
        val value = args[name] ?: return null
        return (value as? JsonPrimitive)?.takeIf { !it.isString }?.booleanOrNull ?: throw ToolRefusal("$NOT_BOOLEAN $name")
    }

    fun obj(args: JsonObject, name: String): JsonObject? {
        val value = args[name] ?: return null
        return value as? JsonObject ?: throw ToolRefusal("$NOT_OBJECT $name")
    }
}

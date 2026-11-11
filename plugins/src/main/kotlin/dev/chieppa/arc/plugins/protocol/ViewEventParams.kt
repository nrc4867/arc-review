package dev.chieppa.arc.plugins.protocol

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray

/** A person used node [node] of view [view]: call its [handler] prop, such as `onClick`, with [args]. */
@Serializable
data class ViewEventParams(val view: String, val node: Int, val handler: String, val args: JsonArray = JsonArray(emptyList()))

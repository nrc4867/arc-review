package dev.chieppa.arc.plugins.protocol

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/** Open the plugin's pane kind [pane] in the grid with [props], for [context]'s source. */
@Serializable
data class PanesOpenParams(val pane: String, val props: JsonObject = JsonObject(emptyMap()), val context: CallContext = CallContext())

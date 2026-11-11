package dev.chieppa.arc.plugins.protocol

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/** Event [name], one of [AppEvents], on [context], with what else it carries in [data]. */
@Serializable
data class AppEventParams(val name: String, val context: CallContext = CallContext(), val data: JsonObject = JsonObject(emptyMap()))

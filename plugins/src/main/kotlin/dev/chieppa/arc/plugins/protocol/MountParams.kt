package dev.chieppa.arc.plugins.protocol

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * Render the plugin's [contribution] of [kind] as the view [view], on [context], with the [props] it was opened with.
 * The settings page has no contribution id.
 */
@Serializable
data class MountParams(
    val view: String,
    val kind: ViewKind,
    val contribution: String = "",
    val context: CallContext = CallContext(),
    val props: JsonObject = JsonObject(emptyMap()),
)

package dev.chieppa.arc.plugins.view

import kotlinx.serialization.json.JsonObject

/**
 * One node of a plugin view as it stands: a component [type] with its [props] and [children], or a run of [text] with
 * [type] [TEXT]. [id] is the plugin's number for it, which events name.
 */
data class ViewNode(
    val id: Int,
    val type: String,
    val props: JsonObject = JsonObject(emptyMap()),
    val children: List<ViewNode> = emptyList(),
    val text: String? = null,
) {
    companion object {
        /** The type of a text node. */
        const val TEXT = "#text"
    }
}

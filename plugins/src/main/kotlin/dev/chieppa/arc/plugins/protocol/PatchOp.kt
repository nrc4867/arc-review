package dev.chieppa.arc.plugins.protocol

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator
import kotlinx.serialization.json.JsonObject

/**
 * One change to a view's tree, as the plugin's React reconciler commits it. Nodes are numbered by the plugin; the view
 * itself is node [ROOT]. A prop that is a function arrives as `{"$handler": true}` and is called through a view event.
 */
@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("op")
sealed interface PatchOp {
    /** A component [type] with [props], not yet in the tree. */
    @Serializable
    @SerialName("create")
    data class Create(val id: Int, val type: String, val props: JsonObject = JsonObject(emptyMap())) : PatchOp

    /** A run of text, not yet in the tree. */
    @Serializable
    @SerialName("text")
    data class CreateText(val id: Int, val text: String) : PatchOp

    /** Node [id]'s props are now [props]. */
    @Serializable
    @SerialName("props")
    data class SetProps(val id: Int, val props: JsonObject) : PatchOp

    /** Text node [id] now reads [text]. */
    @Serializable
    @SerialName("setText")
    data class SetText(val id: Int, val text: String) : PatchOp

    /** [child] goes last under [parent], leaving wherever it was. */
    @Serializable
    @SerialName("append")
    data class Append(val parent: Int, val child: Int) : PatchOp

    /** [child] goes under [parent] just before [before], leaving wherever it was. */
    @Serializable
    @SerialName("insert")
    data class Insert(val parent: Int, val child: Int, val before: Int) : PatchOp

    /** [child] and everything under it leave the tree. */
    @Serializable
    @SerialName("remove")
    data class Remove(val parent: Int, val child: Int) : PatchOp

    companion object {
        /** The id of the view itself, the parent of its top-level nodes. */
        const val ROOT = 0

        /** The key that marks a prop as a function the plugin holds. */
        const val HANDLER = "\$handler"
    }
}

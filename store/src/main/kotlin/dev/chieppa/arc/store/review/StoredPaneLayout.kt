package dev.chieppa.arc.store.review

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** A pane grid's layout as written: its tree of panes, the focused pane and the next free id number. */
@Serializable
data class StoredPaneLayout(val root: StoredPaneNode, val focused: String, val next: Int)

/** One node of a [StoredPaneLayout]. */
@Serializable
sealed interface StoredPaneNode {
    val id: String

    /** A pane of tabs, by pane id, with the one showing. */
    @Serializable
    @SerialName("leaf")
    data class Leaf(override val id: String, val tabs: List<String>, val active: String? = null) : StoredPaneNode

    /** Panes side by side (`Row`) or stacked (`Column`), with each one's share. */
    @Serializable
    @SerialName("split")
    data class Split(override val id: String, val axis: String, val children: List<StoredPaneNode>, val weights: List<Float>) : StoredPaneNode
}

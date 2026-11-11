package dev.chieppa.arc.plugins.protocol

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Where a plugin's view is shown. */
@Serializable
enum class ViewKind {
    /** A drawer with its own rail icon. */
    @SerialName("panel") Panel,

    /** A pane in the grid. */
    @SerialName("pane") Pane,

    /** The plugin's own page under the Plugins section of Settings. */
    @SerialName("settings") Settings,
}

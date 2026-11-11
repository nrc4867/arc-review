package dev.chieppa.arc.plugins.protocol

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Which side of a change a note's lines are on: before it or after it. */
@Serializable
enum class NoteSide {
    @SerialName("old") Old,
    @SerialName("new") New,
}

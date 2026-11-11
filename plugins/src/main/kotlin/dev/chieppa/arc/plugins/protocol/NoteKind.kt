package dev.chieppa.arc.plugins.protocol

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** What a note a plugin reads or adds is: a note kept on this machine, or a comment draft the person may push. */
@Serializable
enum class NoteKind {
    @SerialName("note") Note,
    @SerialName("draft") Draft,
}

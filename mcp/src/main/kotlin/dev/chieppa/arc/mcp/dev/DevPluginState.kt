package dev.chieppa.arc.mcp.dev

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Where a plugin stands, as the development tools report it. */
@Serializable
enum class DevPluginState {
    /** The person has not enabled it. */
    @SerialName("off") Off,

    /** Enabled, but its manifest asks for capabilities the person has not approved. */
    @SerialName("waiting for approval") WaitingForApproval,

    /** Its manifest cannot be read. */
    @SerialName("unreadable") Unreadable,
    @SerialName("starting") Starting,
    @SerialName("running") Running,
    @SerialName("restarting") Restarting,
    @SerialName("failed") Failed,
}

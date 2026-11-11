package dev.chieppa.arc.plugins.protocol

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** The kinds of value a plugin setting holds. */
@Serializable
enum class SettingType {
    @SerialName("text") Text,
    @SerialName("number") Number,
    @SerialName("boolean") Boolean,
    @SerialName("choice") Choice,
    @SerialName("glob") Glob,
    @SerialName("list") List,
    @SerialName("map") Map,

    /** Text kept in the system's keychain, never in `plugins.json`, and given only to the plugin that set it. */
    @SerialName("secret") Secret,
}

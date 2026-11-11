package dev.chieppa.arc.plugins.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Where plugins come from, as the person added it under Settings. */
@Serializable
sealed interface PluginSourceSpec {
    /** A folder holding one plugin, or a `plugins.json` listing several; watched, and reloaded on every save. */
    @Serializable
    @SerialName("directory")
    data class Directory(val path: String) : PluginSourceSpec

    /**
     * A git repo at [ref] (a branch, tag or commit), cloned into Arc's cache and run at the commit it is [pinned] to,
     * which moves only when the person accepts an update. Never watched: its plugins are read-only.
     */
    @Serializable
    @SerialName("git")
    data class Git(val url: String, val ref: String, val pinned: String? = null) : PluginSourceSpec
}

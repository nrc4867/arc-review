package dev.chieppa.arc.plugins.protocol

import kotlinx.serialization.Serializable

/** Run the plugin's action [action] on [context]. */
@Serializable
data class ActionRunParams(val action: String, val context: CallContext = CallContext())

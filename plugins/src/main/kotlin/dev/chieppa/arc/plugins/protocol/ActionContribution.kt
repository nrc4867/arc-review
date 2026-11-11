package dev.chieppa.arc.plugins.protocol

import kotlinx.serialization.Serializable

/** An action a plugin adds: its [id] within the plugin, its [label], default [keys] such as `Ctrl+Shift+T`, and [icon]. */
@Serializable
data class ActionContribution(val id: String, val label: String, val keys: String? = null, val icon: String? = null)

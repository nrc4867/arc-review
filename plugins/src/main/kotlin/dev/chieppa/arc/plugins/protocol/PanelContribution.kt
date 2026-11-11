package dev.chieppa.arc.plugins.protocol

import kotlinx.serialization.Serializable

/** A drawer a plugin adds, with its own rail [icon] and its [title]. */
@Serializable
data class PanelContribution(val id: String, val title: String, val icon: String? = null)

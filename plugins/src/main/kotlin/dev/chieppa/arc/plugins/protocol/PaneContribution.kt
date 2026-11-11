package dev.chieppa.arc.plugins.protocol

import kotlinx.serialization.Serializable

/** A kind of pane a plugin adds to the grid, with its tab's [title] and [icon]. */
@Serializable
data class PaneContribution(val id: String, val title: String, val icon: String? = null)

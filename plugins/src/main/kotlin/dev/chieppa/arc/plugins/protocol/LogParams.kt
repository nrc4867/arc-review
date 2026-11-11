package dev.chieppa.arc.plugins.protocol

import kotlinx.serialization.Serializable

/** A line the plugin printed, on its standard error when [error]. */
@Serializable
data class LogParams(val text: String, val error: Boolean = false)

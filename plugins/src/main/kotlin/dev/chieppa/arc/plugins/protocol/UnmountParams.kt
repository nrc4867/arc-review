package dev.chieppa.arc.plugins.protocol

import kotlinx.serialization.Serializable

/** The view [view] is gone. */
@Serializable
data class UnmountParams(val view: String)

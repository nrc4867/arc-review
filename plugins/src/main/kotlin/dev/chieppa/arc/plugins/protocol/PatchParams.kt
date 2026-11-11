package dev.chieppa.arc.plugins.protocol

import kotlinx.serialization.Serializable

/** Changes to view [view]'s component tree, applied in order. */
@Serializable
data class PatchParams(val view: String, val ops: List<PatchOp>)

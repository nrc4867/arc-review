package dev.chieppa.arc.plugins.typecheck

import kotlinx.serialization.Serializable

/** One type error as the host's check prints it: in [file] of the plugin's folder (empty for none), at [line] from 1. */
@Serializable
data class CheckedError(val file: String, val line: Int, val column: Int, val message: String)

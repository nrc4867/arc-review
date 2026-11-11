package dev.chieppa.arc.plugins.protocol

import kotlinx.serialization.Serializable

/**
 * What a call into a plugin acts on: the [source] in focus, or the one a view was opened for, and the [file] in focus
 * in it. Every call carries one; either may be null when nothing is open.
 */
@Serializable
data class CallContext(val source: SourceInfo? = null, val file: FileInfo? = null)

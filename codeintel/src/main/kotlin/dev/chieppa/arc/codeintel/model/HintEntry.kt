package dev.chieppa.arc.codeintel.model

import kotlinx.serialization.Serializable

/** One tag of a [HintSection]: the [name] it is about, a [type] written with it, its [markdown] and its [line], from 1. */
@Serializable
data class HintEntry(val markdown: String, val line: Int, val name: String? = null, val type: String? = null)

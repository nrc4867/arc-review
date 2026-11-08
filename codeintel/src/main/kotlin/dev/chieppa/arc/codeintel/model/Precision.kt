package dev.chieppa.arc.codeintel.model

import kotlinx.serialization.Serializable

/** How an answer was reached: by matching names in a syntax index, or exactly, by a tool that knows the types. */
@Serializable
enum class Precision { ByName, Exact }

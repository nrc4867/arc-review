package dev.chieppa.arc.codeintel.model

import kotlinx.serialization.Serializable

/** Whether a candidate passed the provider's checks ([Confident]) or matched only by name and was filtered out. */
@Serializable
enum class Confidence { Confident, Filtered }

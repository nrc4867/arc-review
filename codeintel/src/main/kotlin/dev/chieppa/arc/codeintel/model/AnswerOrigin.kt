package dev.chieppa.arc.codeintel.model

import kotlinx.serialization.Serializable

/** Where an answer came from: the [provider]'s id, how [precision] it is, and the [language] it reads. */
@Serializable
data class AnswerOrigin(val provider: String, val precision: Precision, val language: String? = null)

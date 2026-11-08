package dev.chieppa.arc.codeintel.model

import kotlinx.serialization.Serializable

/** A file's outline, its top-level [symbols] in source order, from [origin]. */
@Serializable
data class OutlineAnswer(val symbols: List<OutlineSymbol>, val origin: AnswerOrigin)

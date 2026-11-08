package dev.chieppa.arc.codeintel.model

import kotlinx.serialization.Serializable

/**
 * The usages found in one [source] at [revision], in one [language], from one [origin]: files the change touches
 * first, confident before filtered.
 */
@Serializable
data class UsageGroup(
    val source: String,
    val revision: String,
    val language: String,
    val origin: AnswerOrigin,
    val usages: List<SymbolTarget>,
)

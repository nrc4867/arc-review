package dev.chieppa.arc.codeintel.model

import kotlinx.serialization.Serializable

/**
 * One source open in the workspace, as an answer needs to know it: its [source] ref (`api#412`), the [repo] and the
 * [revision] its files are read at, and the [changedPaths] its change touches, which answers list first.
 */
@Serializable
data class SourceScope(val source: String, val repo: String, val revision: String, val changedPaths: List<String> = emptyList())

package dev.chieppa.arc.store.actions

import kotlinx.serialization.Serializable

/** The run filters, Mine rules, watched workflows and runs, notifications and summary artifact names as written. */
@Serializable
internal data class StoredRunWatchlist(
    val filters: List<StoredRunFilter> = emptyList(),
    val mineRules: List<String>? = null,
    val workflows: List<StoredWorkflow> = emptyList(),
    val runs: List<StoredWatchedRun> = emptyList(),
    val notify: List<String>? = null,
    val summaryArtifacts: Map<String, String> = emptyMap(),
)

@Serializable
internal data class StoredRunFilter(val id: String, val name: String, val query: String = "", val mine: Boolean = false)

@Serializable
internal data class StoredWorkflow(val repo: String, val path: String)

@Serializable
internal data class StoredWatchedRun(val repo: String, val id: Long, val at: Long)

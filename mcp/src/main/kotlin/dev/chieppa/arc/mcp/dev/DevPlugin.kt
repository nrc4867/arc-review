package dev.chieppa.arc.mcp.dev

import kotlinx.serialization.Serializable

/**
 * A plugin as the development tools see it: its [id], [title], the [folder] it is read from, whether it comes from a
 * [directory] source (else git), whether the person [enabled] it, its [state], its [problems] (load errors and the first
 * type errors) with how many [typeErrors] it has in all, the capabilities it [asks] for beyond what was approved, how
 * many lines its log holds, and the [actions], [panels] and [panes] it offers while it runs.
 */
@Serializable
data class DevPlugin(
    val id: String,
    val title: String,
    val folder: String,
    val directory: Boolean,
    val enabled: Boolean,
    val state: DevPluginState,
    val problems: List<DevProblem> = emptyList(),
    val typeErrors: Int = 0,
    val asks: List<String> = emptyList(),
    val logLines: Int = 0,
    val actions: List<String> = emptyList(),
    val panels: List<String> = emptyList(),
    val panes: List<String> = emptyList(),
)

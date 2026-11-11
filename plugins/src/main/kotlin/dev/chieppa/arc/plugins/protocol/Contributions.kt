package dev.chieppa.arc.plugins.protocol

import kotlinx.serialization.Serializable

/**
 * What a plugin adds to Arc: its [actions], [panels] and [panes], the [settings] it keeps by key and whether it draws
 * its own [settingsPage].
 */
@Serializable
data class Contributions(
    val actions: List<ActionContribution> = emptyList(),
    val panels: List<PanelContribution> = emptyList(),
    val panes: List<PaneContribution> = emptyList(),
    val settings: Map<String, SettingSpec> = emptyMap(),
    val settingsPage: Boolean = false,
)

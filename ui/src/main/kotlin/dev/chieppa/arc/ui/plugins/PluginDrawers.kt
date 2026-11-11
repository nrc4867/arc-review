package dev.chieppa.arc.ui.plugins

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import dev.chieppa.arc.plugins.protocol.ViewKind
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.frame.DrawerPlacement
import dev.chieppa.stretch.frame.DrawerSpec
import dev.chieppa.stretch.frame.Side

/**
 * The drawers of running plugins' panels, each with its own icon in the rail, showing the panel for the active source.
 * A plugin's drawer first opens on the right rail, closed.
 */
object PluginDrawers {
    fun specs(workbench: Workbench): List<DrawerSpec> = workbench.plugins.running.flatMap { (entry, contributions) ->
        contributions.panels.map { panel ->
            DrawerSpec(PluginIds.drawer(entry.id, panel.id), panel.title, PluginIconNames.orPlugin(panel.icon), scrolls = false) {
                val context = PluginContexts.of(workbench, workbench.state.active)
                PluginViewHost(workbench, entry.id, ViewKind.Panel, panel.id, context, Modifier.fillMaxSize())
            }
        }
    }

    /** Where a plugin's drawer goes the first time it shows. */
    fun place(spec: DrawerSpec): DrawerPlacement = DrawerPlacement(spec.id, Side.Right)
}

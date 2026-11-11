package dev.chieppa.arc.ui.plugins.settings

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.chieppa.arc.plugins.protocol.ViewKind
import dev.chieppa.arc.ui.plugins.PluginContexts
import dev.chieppa.arc.ui.plugins.PluginViewHost
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.theme.StretchSpacing

/** Plugin [plugin]'s own settings page, drawn by the plugin with the same components as its panels. */
@Composable
fun PluginSettingsPage(workbench: Workbench, plugin: String) {
    PluginViewHost(
        workbench,
        plugin,
        ViewKind.Settings,
        "",
        PluginContexts.of(workbench, workbench.state.active),
        Modifier.fillMaxWidth().padding(horizontal = StretchSpacing.space14, vertical = StretchSpacing.space10),
    )
}

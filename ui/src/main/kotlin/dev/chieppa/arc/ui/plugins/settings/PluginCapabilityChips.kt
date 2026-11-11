package dev.chieppa.arc.ui.plugins.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.runtime.Composable
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.plugins_capability_actions
import dev.chieppa.arc.design.generated.plugins_capability_console
import dev.chieppa.arc.design.generated.plugins_capability_git
import dev.chieppa.arc.design.generated.plugins_capability_mcp
import dev.chieppa.arc.design.generated.plugins_capability_network
import dev.chieppa.arc.design.generated.plugins_capability_panels
import dev.chieppa.arc.design.generated.plugins_capability_panes
import dev.chieppa.arc.design.generated.plugins_capability_processes
import dev.chieppa.arc.design.generated.plugins_capability_read
import dev.chieppa.arc.design.generated.plugins_capability_write
import dev.chieppa.arc.plugins.manifest.Capabilities
import dev.chieppa.arc.plugins.manifest.Capability
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.Chip
import org.jetbrains.compose.resources.stringResource

/** What a plugin's manifest lets it do, as chips in the code face, shown before any of its code runs. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PluginCapabilityChips(capabilities: Capabilities) {
    val all = Capability.of(capabilities)
    if (all.isEmpty()) return
    FlowRow(horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space4), verticalArrangement = Arrangement.spacedBy(StretchSpacing.space4)) {
        all.forEach { Chip(labelOf(it), mono = true) }
    }
}

@Composable
private fun labelOf(capability: Capability): String = when (capability) {
    Capability.Panels -> stringResource(Res.string.plugins_capability_panels)
    Capability.Panes -> stringResource(Res.string.plugins_capability_panes)
    Capability.Actions -> stringResource(Res.string.plugins_capability_actions)
    Capability.Console -> stringResource(Res.string.plugins_capability_console)
    Capability.Git -> stringResource(Res.string.plugins_capability_git)
    Capability.Processes -> stringResource(Res.string.plugins_capability_processes)
    is Capability.ReadFiles -> stringResource(Res.string.plugins_capability_read, capability.path)
    is Capability.WriteFiles -> stringResource(Res.string.plugins_capability_write, capability.path)
    is Capability.Network -> stringResource(Res.string.plugins_capability_network, capability.host)
    is Capability.Mcp -> stringResource(Res.string.plugins_capability_mcp, capability.role)
}

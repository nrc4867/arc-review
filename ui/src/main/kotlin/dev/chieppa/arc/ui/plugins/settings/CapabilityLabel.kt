package dev.chieppa.arc.ui.plugins.settings

import androidx.compose.runtime.Composable
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.plugins_capability_actions
import dev.chieppa.arc.design.generated.plugins_capability_comments_draft
import dev.chieppa.arc.design.generated.plugins_capability_console
import dev.chieppa.arc.design.generated.plugins_capability_git
import dev.chieppa.arc.design.generated.plugins_capability_mcp
import dev.chieppa.arc.design.generated.plugins_capability_network
import dev.chieppa.arc.design.generated.plugins_capability_notes_read
import dev.chieppa.arc.design.generated.plugins_capability_notes_write
import dev.chieppa.arc.design.generated.plugins_capability_panels
import dev.chieppa.arc.design.generated.plugins_capability_panes
import dev.chieppa.arc.design.generated.plugins_capability_read
import dev.chieppa.arc.design.generated.plugins_capability_write
import dev.chieppa.arc.plugins.manifest.Capability
import org.jetbrains.compose.resources.stringResource

/** How a plugin's row names [capability], in the code face. */
@Composable
fun capabilityLabel(capability: Capability): String = when (capability) {
    Capability.Panels -> stringResource(Res.string.plugins_capability_panels)
    Capability.Panes -> stringResource(Res.string.plugins_capability_panes)
    Capability.Actions -> stringResource(Res.string.plugins_capability_actions)
    Capability.Console -> stringResource(Res.string.plugins_capability_console)
    Capability.Git -> stringResource(Res.string.plugins_capability_git)
    Capability.ReadNotes -> stringResource(Res.string.plugins_capability_notes_read)
    Capability.WriteNotes -> stringResource(Res.string.plugins_capability_notes_write)
    Capability.DraftComments -> stringResource(Res.string.plugins_capability_comments_draft)
    is Capability.ReadFiles -> stringResource(Res.string.plugins_capability_read, capability.path)
    is Capability.WriteFiles -> stringResource(Res.string.plugins_capability_write, capability.path)
    is Capability.Network -> stringResource(Res.string.plugins_capability_network, capability.host)
    is Capability.Mcp -> stringResource(Res.string.plugins_capability_mcp, capability.role)
}

package dev.chieppa.arc.ui.mcp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.mcp_clients
import dev.chieppa.arc.design.generated.mcp_connect
import dev.chieppa.arc.design.generated.mcp_copy
import dev.chieppa.arc.design.generated.mcp_failed
import dev.chieppa.arc.design.generated.mcp_listening
import dev.chieppa.arc.design.generated.mcp_off
import dev.chieppa.arc.design.generated.mcp_starting
import dev.chieppa.arc.design.generated.mcp_switch
import dev.chieppa.arc.design.generated.mcp_token_not_kept
import dev.chieppa.arc.mcp.server.ConnectCommand
import dev.chieppa.arc.mcp.server.McpServerState
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.Button
import dev.chieppa.stretch.ui.ButtonKind
import dev.chieppa.stretch.ui.Chip
import dev.chieppa.stretch.ui.ChipKind
import dev.chieppa.stretch.ui.Divider
import dev.chieppa.stretch.ui.Label
import dev.chieppa.stretch.ui.Toggle
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/**
 * The MCP server's card: its switch with where it stands beside it (off, listening on its address, how many clients are
 * connected, or why it is not listening, highlighted), and while it is on, the command that connects Claude Code with
 * its token, and a button copying it. A token that lasts only until Arc closes is highlighted.
 */
@Composable
fun McpServerCard(workbench: Workbench) {
    val mcp = workbench.mcp
    val state = mcp.state
    val c = Stretch.colors
    Column {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = StretchSpacing.space14, vertical = StretchSpacing.space12),
            horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space10),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val switchText = stringResource(Res.string.mcp_switch)
            Label(switchText, Stretch.type.body, c.inkStrong, Modifier.weight(1f))
            StateChip(state)
            Toggle(state != McpServerState.Off, mcp::switch, switchText, Modifier.testTag(McpSettingsTags.SWITCH))
        }
        val command = mcp.command().takeIf { state != McpServerState.Off } ?: return@Column
        Divider()
        Column(
            Modifier.fillMaxWidth().padding(horizontal = StretchSpacing.space14, vertical = StretchSpacing.space12),
            verticalArrangement = Arrangement.spacedBy(StretchSpacing.space8),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space10), verticalAlignment = Alignment.CenterVertically) {
                Label(stringResource(Res.string.mcp_connect), Stretch.type.body, c.inkStrong, Modifier.weight(1f))
                if (!mcp.tokenLasts) Chip(stringResource(Res.string.mcp_token_not_kept), kind = ChipKind.Warn)
                Button(stringResource(Res.string.mcp_copy), { workbench.clipboard.copy(command) }, Modifier.testTag(McpSettingsTags.COPY), kind = ButtonKind.Quiet)
            }
            BasicText(command, Modifier.testTag(McpSettingsTags.COMMAND), Stretch.type.valueSm.merge(TextStyle(color = c.inkMuted)))
        }
    }
}

@Composable
private fun StateChip(state: McpServerState) {
    when (state) {
        McpServerState.Off -> Chip(stringResource(Res.string.mcp_off))
        McpServerState.Starting -> Chip(stringResource(Res.string.mcp_starting))
        is McpServerState.Failed -> Chip(stringResource(Res.string.mcp_failed, state.reason), kind = ChipKind.Warn)
        is McpServerState.Listening ->
            if (state.clients > 0) {
                Chip(pluralStringResource(Res.plurals.mcp_clients, state.clients, state.clients), kind = ChipKind.On)
            } else {
                Chip(stringResource(Res.string.mcp_listening, ConnectCommand.url(state.port)), mono = true)
            }
    }
}

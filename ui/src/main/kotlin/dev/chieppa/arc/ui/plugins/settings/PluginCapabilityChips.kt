package dev.chieppa.arc.ui.plugins.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.runtime.Composable
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.plugins_asks
import dev.chieppa.arc.design.generated.plugins_risk_fetches_into_files
import dev.chieppa.arc.design.generated.plugins_risk_reads_and_sends
import dev.chieppa.arc.design.generated.plugins_risk_runs_programs
import dev.chieppa.arc.plugins.manifest.Capabilities
import dev.chieppa.arc.plugins.manifest.Capability
import dev.chieppa.arc.plugins.manifest.CapabilityRisk
import dev.chieppa.arc.plugins.manifest.RiskKind
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.Chip
import dev.chieppa.stretch.ui.ChipKind
import org.jetbrains.compose.resources.stringResource

/**
 * What a plugin's manifest lets it do, as chips in the code face, shown before any of its code runs. While [flagged],
 * as when it is off or an update asks for more, the capabilities it [asks] for anew and those in a risky combination
 * are warning chips, each naming why for assistive tools.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PluginCapabilityChips(capabilities: Capabilities, asks: List<Capability>, flagged: Boolean) {
    val all = Capability.of(capabilities)
    if (all.isEmpty()) return
    val risks = if (flagged) CapabilityRisk.of(capabilities) else emptyList()
    FlowRow(horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space4), verticalArrangement = Arrangement.spacedBy(StretchSpacing.space4)) {
        all.forEach { capability ->
            val label = capabilityLabel(capability)
            val risk = risks.firstOrNull { capability in it.capabilities }
            val asked = flagged && capability in asks
            val description = when {
                asked -> stringResource(Res.string.plugins_asks, label)
                risk != null -> riskOf(risk.kind, label)
                else -> null
            }
            Chip(label, kind = if (description != null) ChipKind.Warn else ChipKind.Default, mono = true, description = description)
        }
    }
}

@Composable
private fun riskOf(kind: RiskKind, label: String): String = when (kind) {
    RiskKind.ReadsAndSends -> stringResource(Res.string.plugins_risk_reads_and_sends, label)
    RiskKind.FetchesIntoFiles -> stringResource(Res.string.plugins_risk_fetches_into_files, label)
    RiskKind.RunsPrograms -> stringResource(Res.string.plugins_risk_runs_programs, label)
}

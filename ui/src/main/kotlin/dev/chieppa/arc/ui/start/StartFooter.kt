package dev.chieppa.arc.ui.start

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.chieppa.arc.core.start.StartLink
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.Button
import dev.chieppa.stretch.ui.ButtonKind

/** A start card's footer: its [links] as quiet buttons. */
@Composable
fun StartFooter(workbench: Workbench, links: List<StartLink>) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = StretchSpacing.space6, vertical = StretchSpacing.space4),
        horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space4),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        for (link in links) {
            Button(link.label, { StartRunner.run(workbench, link.action) }, kind = ButtonKind.Quiet)
        }
    }
}

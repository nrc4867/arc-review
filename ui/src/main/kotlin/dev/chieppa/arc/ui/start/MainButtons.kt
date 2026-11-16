package dev.chieppa.arc.ui.start

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.chieppa.arc.core.start.StartButton
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.Button
import dev.chieppa.stretch.ui.ButtonKind

/** The start screen's main buttons, each running its action. Nothing when there are none. */
@Composable
fun MainButtons(workbench: Workbench, buttons: List<StartButton>) {
    if (buttons.isEmpty()) return
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space10),
        verticalArrangement = Arrangement.spacedBy(StretchSpacing.space8),
        itemVerticalAlignment = Alignment.CenterVertically,
    ) {
        for (button in buttons) {
            Button(
                button.label,
                { StartRunner.run(workbench, button.action) },
                Modifier.height(StartSizes.goButton),
                kind = if (button.primary) ButtonKind.Primary else ButtonKind.Default,
                icon = StartIcons.of(button.icon),
            )
        }
    }
}

package dev.chieppa.arc.ui.runs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import dev.chieppa.arc.core.actions.RunStep
import dev.chieppa.arc.ui.util.bottomRule
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchSpacing

/** One step of a finished job: its state, name and time, in the on fill when picked. */
@Composable
fun StepRow(step: RunStep, now: Long, selected: Boolean, onClick: () -> Unit) {
    val c = Stretch.colors
    Row(
        Modifier.fillMaxWidth().height(RunSizes.step).background(if (selected) c.onFill else Color.Transparent).bottomRule(c.chartGrid)
            .clickable(role = Role.Button, onClick = onClick).semantics { this.selected = selected }.padding(horizontal = StretchSpacing.space14),
        horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space10),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RunStateIcon(step.state)
        RunText(step.name, RunTextKind.Body, c.ink, Modifier.weight(1f))
        RunText(step.seconds(now)?.let(RunDurations::of).orEmpty(), RunTextKind.MonoSmall, c.inkMuted)
    }
}

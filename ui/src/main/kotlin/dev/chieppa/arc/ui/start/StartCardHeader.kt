package dev.chieppa.arc.ui.start

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import dev.chieppa.arc.core.start.StartCard
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.theme.Icon
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.Button
import dev.chieppa.stretch.ui.ButtonKind
import dev.chieppa.stretch.ui.ControlLabel

/** A start card's header: its icon and title, and its header link as a quiet button when it can run. */
@Composable
fun StartCardHeader(workbench: Workbench, card: StartCard) {
    val c = Stretch.colors
    Row(
        Modifier.fillMaxWidth().heightIn(min = StartSizes.cardHeader).padding(start = StretchSpacing.space12, end = StretchSpacing.space6),
        horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space8),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StartIcons.of(card.icon)?.let { Icon(it, c.inkControl) }
        ControlLabel(card.title, Stretch.type.body.copy(fontWeight = FontWeight.Medium), c.inkStrong, Modifier.weight(1f))
        card.header?.takeIf { StartRunner.available(workbench, it.action) }?.let { link ->
            Button(link.label, { StartRunner.run(workbench, link.action) }, kind = ButtonKind.Quiet)
        }
    }
}

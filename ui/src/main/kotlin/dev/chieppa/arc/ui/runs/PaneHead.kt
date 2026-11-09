package dev.chieppa.arc.ui.runs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import dev.chieppa.arc.ui.util.ArcSizes
import dev.chieppa.arc.ui.util.bottomRule
import dev.chieppa.stretch.theme.Icon
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchSpacing

/** The head of a column in a run's page: an icon and [title], [detail] at the end, and [trailing] controls after it. */
@Composable
fun PaneHead(icon: ImageVector, title: String, detail: String, trailing: @Composable () -> Unit = {}) {
    val c = Stretch.colors
    Row(
        Modifier.fillMaxWidth().heightIn(min = RunSizes.step).background(c.surfacePanel).bottomRule(c.linePanel).padding(horizontal = StretchSpacing.space12),
        horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space8),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, c.inkMuted, size = ArcSizes.pullIcon)
        RunText(title, RunTextKind.Strong, c.inkStrong, Modifier.weight(1f, fill = false))
        RunText(detail, RunTextKind.Small, c.inkSubtle, Modifier.weight(1f), align = androidx.compose.ui.text.style.TextAlign.End)
        trailing()
    }
}

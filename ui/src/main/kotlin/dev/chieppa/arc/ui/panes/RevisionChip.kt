package dev.chieppa.arc.ui.panes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import dev.chieppa.arc.core.pane.Pane
import dev.chieppa.arc.design.ArcIcons
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.pane_revision_range
import dev.chieppa.arc.ui.util.ArcSizes
import dev.chieppa.arc.ui.util.Revisions
import dev.chieppa.stretch.theme.Icon
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchSpacing
import org.jetbrains.compose.resources.stringResource

/** The revisions a pane shows: `base → head` for a change, or the one revision a whole file is at. */
@Composable
fun RevisionChip(pane: Pane, modifier: Modifier = Modifier) {
    val c = Stretch.colors
    val base = pane.base
    val text = if (base != null) {
        stringResource(Res.string.pane_revision_range, Revisions.short(base), Revisions.short(pane.revision))
    } else {
        Revisions.short(pane.revision)
    }
    val icon = when {
        base != null -> ArcIcons.Range
        Revisions.isSha(pane.revision) -> ArcIcons.Commit
        else -> ArcIcons.Branch
    }
    Row(
        modifier.height(ArcSizes.revisionChipHeight).clip(Stretch.shapes.pillShape).background(c.surfaceWell).padding(horizontal = StretchSpacing.space8),
        horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space4),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, c.inkMuted, size = ArcSizes.ring)
        BasicText(text, style = Stretch.type.valueSm.merge(TextStyle(color = c.ink)), maxLines = 1)
    }
}

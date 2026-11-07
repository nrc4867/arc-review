package dev.chieppa.arc.ui.revisions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import dev.chieppa.arc.core.change.CommitInfo
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.revisions_number
import dev.chieppa.arc.design.generated.revisions_pick
import dev.chieppa.arc.ui.util.ArcSizes
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.Checkbox
import org.jetbrains.compose.resources.stringResource

/** One commit in the Commits drawer: a box that picks it for comparing, its headline, short sha and the revision that brought it. */
@Composable
fun CommitRow(commit: CommitInfo, revision: Int, picked: Boolean, onPick: () -> Unit) {
    val c = Stretch.colors
    val shape = Stretch.shapes.rowShape
    Checkbox(
        picked,
        { onPick() },
        stringResource(Res.string.revisions_pick),
        Modifier.fillMaxWidth().heightIn(min = ArcSizes.commitRow).clip(shape).background(if (picked) c.surfaceControl else Color.Transparent)
            .padding(horizontal = StretchSpacing.space2),
    ) {
        Column(Modifier.weight(1f)) {
            BasicText(commit.subject, style = Stretch.type.body.merge(TextStyle(color = c.ink)), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space6), verticalAlignment = Alignment.CenterVertically) {
                BasicText(commit.id.short(), style = Stretch.type.valueSm.merge(TextStyle(color = c.inkMuted)))
                BasicText(stringResource(Res.string.revisions_number, revision), style = Stretch.type.bodySm.merge(TextStyle(color = c.inkSubtle)))
            }
        }
    }
}

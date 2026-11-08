package dev.chieppa.arc.ui.conversation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.ui.review.WorkspaceState
import dev.chieppa.arc.ui.sources.sourceIcon
import dev.chieppa.arc.ui.util.ArcSizes
import dev.chieppa.arc.ui.util.SourceLabels
import dev.chieppa.stretch.theme.Icon
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchSpacing

/** The source a drawer is about, at its top: its icon, ref and title, with [meta] under it when given. */
@Composable
fun SourceHeading(state: WorkspaceState, source: Source, modifier: Modifier = Modifier, meta: @Composable (() -> Unit)? = null) {
    val c = Stretch.colors
    Column(modifier, verticalArrangement = Arrangement.spacedBy(StretchSpacing.space6)) {
        Row(horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space8), verticalAlignment = Alignment.CenterVertically) {
            Icon(sourceIcon(source), c.inkMuted, size = ArcSizes.pullIcon)
            BasicText(SourceLabels.of(source), style = Stretch.type.valueSm.merge(TextStyle(color = c.inkStrong)), maxLines = 1)
            val title = state.pulls[source]?.title ?: state.snapshot(source)?.title.orEmpty()
            BasicText(title, Modifier.weight(1f), style = Stretch.type.bodySm.merge(TextStyle(color = c.inkMuted)), maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        meta?.invoke()
    }
}

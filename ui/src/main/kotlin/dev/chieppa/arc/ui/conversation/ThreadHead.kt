package dev.chieppa.arc.ui.conversation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import dev.chieppa.arc.core.conversation.ReviewThread
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.thread_fold
import dev.chieppa.arc.design.generated.thread_line
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchIcons
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.arc.ui.util.TipIconButton
import org.jetbrains.compose.resources.stringResource

/**
 * A thread card's head: its state chip, its line with [where] after it, or the first message's [peek] while folded,
 * then [trailing] and the fold button. A click anywhere on it folds or unfolds the card; its text is not selectable.
 */
@Composable
fun ThreadHead(
    thread: ReviewThread,
    expanded: Boolean,
    onToggle: () -> Unit,
    where: String,
    peek: String?,
    trailing: @Composable RowScope.() -> Unit = {},
) {
    val c = Stretch.colors
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onToggle).padding(horizontal = StretchSpacing.space10, vertical = StretchSpacing.space6),
        horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space8),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DisableSelection {
            val (text, icon) = threadChip(thread.state)
            StateChip(text, icon, stateLook(thread.state))
            val muted = Stretch.type.valueSm.merge(TextStyle(color = c.inkMuted))
            val line = stringResource(Res.string.thread_line, thread.line)
            val summary = if (expanded || peek == null) "$line · $where" else "$line  $peek"
            BasicText(summary, Modifier.weight(1f), style = muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            trailing()
            TipIconButton(if (expanded) StretchIcons.ChevronDown else StretchIcons.ChevronRight, stringResource(Res.string.thread_fold), onToggle)
        }
    }
}

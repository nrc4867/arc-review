package dev.chieppa.arc.ui.revisions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import dev.chieppa.arc.core.conversation.ThreadState
import dev.chieppa.arc.core.revision.ThreadCounts
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.matrix_count_addressed
import dev.chieppa.arc.design.generated.matrix_count_open
import dev.chieppa.arc.design.generated.matrix_count_resolved
import dev.chieppa.arc.ui.util.ArcSizes
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.ui.Tooltip
import org.jetbrains.compose.resources.stringResource

/** A cell's thread counts: a dot and a number for the open, addressed and resolved threads it has, in [ink]. */
@Composable
fun ThreadDots(counts: ThreadCounts, ink: Color) {
    if (counts.open > 0) ThreadCount(ThreadState.Open, counts.open, stringResource(Res.string.matrix_count_open, counts.open), ink)
    if (counts.addressed > 0) ThreadCount(ThreadState.Addressed, counts.addressed, stringResource(Res.string.matrix_count_addressed, counts.addressed), ink)
    if (counts.resolved > 0) ThreadCount(ThreadState.Resolved, counts.resolved, stringResource(Res.string.matrix_count_resolved, counts.resolved), ink)
}

@Composable
private fun ThreadCount(state: ThreadState, count: Int, label: String, ink: Color) {
    Tooltip(label) {
        Row(horizontalArrangement = Arrangement.spacedBy(ArcSizes.threadCountGap), verticalAlignment = Alignment.CenterVertically) {
            ThreadDot(state)
            BasicText(count.toString(), style = Stretch.type.valueSm.merge(TextStyle(color = ink)))
        }
    }
}

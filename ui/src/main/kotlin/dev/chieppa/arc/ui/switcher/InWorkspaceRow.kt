package dev.chieppa.arc.ui.switcher

import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.sources_progress
import dev.chieppa.arc.design.generated.switcher_commits
import dev.chieppa.arc.design.generated.switcher_meta
import dev.chieppa.arc.ui.pulls.CheckMark
import dev.chieppa.arc.ui.review.WorkspaceState
import dev.chieppa.arc.ui.sources.ProgressRing
import dev.chieppa.arc.ui.sources.sourceIcon
import dev.chieppa.arc.ui.util.ArcSizes
import dev.chieppa.arc.ui.util.SourceLabels
import dev.chieppa.stretch.frame.PaletteRow
import dev.chieppa.stretch.theme.Icon
import dev.chieppa.stretch.theme.Stretch
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/**
 * A source in the strip, in the switcher: its kind, title and ref, who made it and how many commits it has, how much
 * of it is reviewed, and its checks.
 */
@Composable
fun InWorkspaceRow(item: SwitcherItem.InWorkspace, state: WorkspaceState, query: String, highlighted: Boolean, onClick: () -> Unit) {
    val c = Stretch.colors
    val source = item.source
    val snapshot = state.snapshot(source)
    val pull = state.pulls[source]
    val author = pull?.author ?: snapshot?.commits?.lastOrNull()?.author
    val commits = snapshot?.commits?.size ?: 0
    val meta = when {
        author == null -> ""
        commits > 1 -> stringResource(Res.string.switcher_meta, author, pluralStringResource(Res.plurals.switcher_commits, commits, commits))
        else -> author
    }
    PaletteRow(highlighted, onClick) {
        Icon(sourceIcon(source), c.inkControl)
        if (item.title.isNotEmpty()) RowTitle(item.title, query)
        RowRef(SourceLabels.of(source))
        RowMeta(meta, Modifier.weight(1f))
        state.progress(source)?.let { progress ->
            ProgressRing(if (progress.total == 0) 1f else progress.reviewed.toFloat() / progress.total, size = ArcSizes.switcherRing)
            BasicText(
                stringResource(Res.string.sources_progress, progress.reviewed, progress.total),
                style = Stretch.type.valueSm.merge(TextStyle(color = c.inkMuted)),
            )
        }
        pull?.checks?.state?.let { CheckMark(it) }
    }
}

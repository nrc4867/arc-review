package dev.chieppa.arc.ui.switcher

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.chieppa.arc.ui.sources.sourceIcon
import dev.chieppa.arc.ui.util.SourceLabels
import dev.chieppa.stretch.frame.PaletteRow
import dev.chieppa.stretch.theme.Icon
import dev.chieppa.stretch.theme.Stretch

/** A commit or range of a followed repo in the switcher: its title with what [query] matched, its ref, who made it and when. */
@Composable
fun FollowedCommitRow(item: SwitcherItem.FollowedCommit, query: String, nowSeconds: Long, highlighted: Boolean, onClick: () -> Unit) {
    PaletteRow(highlighted, onClick) {
        Icon(sourceIcon(item.source), Stretch.colors.inkControl)
        RowTitle(item.commit.subject, query)
        RowRef(SourceLabels.of(item.source))
        RowMeta(item.commit.author, Modifier.weight(1f))
        RowMeta(agoText(item.commit.committed, nowSeconds))
    }
}

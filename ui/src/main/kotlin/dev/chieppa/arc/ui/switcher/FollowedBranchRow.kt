package dev.chieppa.arc.ui.switcher

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.chieppa.arc.design.ArcIcons
import dev.chieppa.stretch.frame.PaletteRow
import dev.chieppa.stretch.theme.Icon
import dev.chieppa.stretch.theme.Stretch

/** A local branch of a followed repo in the switcher: its name with what [query] matched, its repo, who last committed and when. */
@Composable
fun FollowedBranchRow(item: SwitcherItem.FollowedBranch, query: String, nowSeconds: Long, highlighted: Boolean, onClick: () -> Unit) {
    val c = Stretch.colors
    PaletteRow(highlighted, onClick) {
        Icon(ArcIcons.Branch, c.inkControl)
        RowTitle(item.branch.name, query)
        RowRef(item.source.repo.value)
        RowMeta(item.branch.author, Modifier.weight(1f))
        RowMeta(agoText(item.branch.committed, nowSeconds))
    }
}

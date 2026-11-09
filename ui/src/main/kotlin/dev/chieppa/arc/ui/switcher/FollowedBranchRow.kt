package dev.chieppa.arc.ui.switcher

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.chieppa.arc.design.ArcIcons
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.switcher_ahead
import org.jetbrains.compose.resources.pluralStringResource
import dev.chieppa.stretch.frame.PaletteRow
import dev.chieppa.stretch.theme.Icon
import dev.chieppa.stretch.theme.Stretch

/**
 * A local branch of a followed repo in the switcher: its name with what [query] matched, its repo, who last committed,
 * how many commits it has ahead of the default branch, and when.
 */
@Composable
fun FollowedBranchRow(item: SwitcherItem.FollowedBranch, query: String, nowSeconds: Long, highlighted: Boolean, onClick: () -> Unit) {
    val c = Stretch.colors
    PaletteRow(highlighted, onClick) {
        Icon(ArcIcons.Branch, c.inkControl)
        RowTitle(item.branch.name, query)
        RowRef(item.source.repo.value)
        RowMeta(item.branch.author, Modifier.weight(1f))
        item.branch.ahead?.let { ahead ->
            RowMeta(pluralStringResource(Res.plurals.switcher_ahead, ahead, ahead), color = if (ahead == 0) c.inkSubtle else c.inkMuted)
        }
        RowMeta(agoText(item.branch.committed, nowSeconds))
    }
}

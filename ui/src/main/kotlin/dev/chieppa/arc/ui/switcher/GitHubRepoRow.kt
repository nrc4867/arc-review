package dev.chieppa.arc.ui.switcher

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import dev.chieppa.arc.core.activity.failureHeadline
import dev.chieppa.arc.design.ArcIcons
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.pull_cloning
import dev.chieppa.arc.design.generated.switcher_add_repo
import dev.chieppa.arc.design.generated.switcher_search_repo
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.frame.PaletteRow
import dev.chieppa.stretch.theme.Icon
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.ui.Spinner
import dev.chieppa.stretch.ui.Tooltip
import org.jetbrains.compose.resources.stringResource

/**
 * A GitHub repo named in the switcher's field: adding it, which clones it, with a spinner while it clones and the error
 * ink when it could not be, git's reason beside it; or, when it is followed already, searching its sources.
 */
@Composable
fun GitHubRepoRow(item: SwitcherItem.GitHubRepository, workbench: Workbench, highlighted: Boolean, onClick: () -> Unit) {
    val c = Stretch.colors
    val failed = item.local == null && item.repo in workbench.cloneFailed
    val reason = if (failed) workbench.cloneFailed[item.repo] else null
    val ink = if (failed) c.errorInk else c.inkMuted
    PaletteRow(highlighted, onClick) {
        Icon(ArcIcons.Repo, if (failed) c.errorInk else c.inkControl)
        RowTitle(stringResource(if (item.local == null) Res.string.switcher_add_repo else Res.string.switcher_search_repo), "")
        RowRef(item.repo.fullName, Modifier.weight(1f, fill = false), color = ink)
        if (reason != null) {
            Box(Modifier.weight(1f)) {
                Tooltip(reason.trim()) {
                    BasicText(failureHeadline(reason), style = Stretch.type.chip.merge(TextStyle(color = c.errorInk)), maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
        if (item.repo in workbench.cloning) Spinner(stringResource(Res.string.pull_cloning, item.repo.fullName))
    }
}

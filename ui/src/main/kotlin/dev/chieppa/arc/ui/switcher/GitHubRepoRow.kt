package dev.chieppa.arc.ui.switcher

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
import org.jetbrains.compose.resources.stringResource

/**
 * A GitHub repo named in the switcher's field: adding it, which clones it, with a spinner while it clones and the error
 * ink when it could not be; or, when it is followed already, searching its sources.
 */
@Composable
fun GitHubRepoRow(item: SwitcherItem.GitHubRepository, workbench: Workbench, highlighted: Boolean, onClick: () -> Unit) {
    val c = Stretch.colors
    val failed = item.local == null && item.repo in workbench.cloneFailed
    val ink = if (failed) c.errorInk else c.inkMuted
    PaletteRow(highlighted, onClick) {
        Icon(ArcIcons.Repo, if (failed) c.errorInk else c.inkControl)
        RowTitle(stringResource(if (item.local == null) Res.string.switcher_add_repo else Res.string.switcher_search_repo), "")
        RowRef(item.repo.fullName, Modifier.weight(1f, fill = false), color = ink)
        if (item.repo in workbench.cloning) Spinner(stringResource(Res.string.pull_cloning, item.repo.fullName))
    }
}

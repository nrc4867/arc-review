package dev.chieppa.arc.ui.switcher

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.chieppa.arc.design.ArcIcons
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.switcher_draft
import dev.chieppa.arc.design.generated.switcher_meta
import dev.chieppa.arc.design.generated.switcher_waiting_you
import dev.chieppa.arc.host.github.pulls.Viewer
import dev.chieppa.arc.host.github.pulls.WaitingOn
import dev.chieppa.arc.host.github.pulls.WaitingOnRule
import dev.chieppa.arc.ui.pulls.CheckMark
import dev.chieppa.arc.ui.util.SourceLabels
import dev.chieppa.stretch.frame.PaletteRow
import dev.chieppa.stretch.theme.Icon
import dev.chieppa.stretch.theme.Stretch
import org.jetbrains.compose.resources.stringResource

/**
 * An open pull request of a followed repo in the switcher: its title with what [query] matched, its ref, its author,
 * whether it is a draft or waits on the person ([viewer], by the Pull requests drawer's rule), and its checks.
 */
@Composable
fun FollowedPullRow(item: SwitcherItem.FollowedPull, viewer: Viewer?, query: String, highlighted: Boolean, onClick: () -> Unit) {
    val c = Stretch.colors
    val pull = item.pull
    val state = when {
        pull.draft -> stringResource(Res.string.switcher_draft)
        WaitingOnRule.of(pull, viewer) is WaitingOn.You -> stringResource(Res.string.switcher_waiting_you)
        else -> null
    }
    val author = pull.author.orEmpty()
    val meta = if (state != null && author.isNotEmpty()) stringResource(Res.string.switcher_meta, author, state) else state ?: author
    PaletteRow(highlighted, onClick) {
        Icon(ArcIcons.PullRequest, c.inkControl)
        RowTitle(pull.title, query)
        RowRef(SourceLabels.of(item.source))
        RowMeta(meta, Modifier.weight(1f))
        CheckMark(pull.checks.state)
    }
}

package dev.chieppa.arc.ui.switcher

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.TextStyle
import dev.chieppa.arc.core.pane.Revision
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.switcher_files_placeholder
import dev.chieppa.arc.design.generated.switcher_link_branch
import dev.chieppa.arc.design.generated.switcher_link_commit
import dev.chieppa.arc.design.generated.switcher_link_pull
import dev.chieppa.arc.design.generated.switcher_link_range
import dev.chieppa.arc.design.generated.switcher_placeholder
import dev.chieppa.arc.design.generated.switcher_ref_branch
import dev.chieppa.arc.design.generated.switcher_ref_commit
import dev.chieppa.arc.design.generated.switcher_ref_pull
import dev.chieppa.arc.design.generated.switcher_ref_range
import dev.chieppa.arc.design.generated.switcher_results
import dev.chieppa.arc.design.generated.switcher_sources_placeholder
import dev.chieppa.arc.ui.sources.sourceIcon
import dev.chieppa.arc.ui.util.ArcSizes
import dev.chieppa.arc.ui.util.Revisions
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.frame.SearchPalette
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchIcons
import dev.chieppa.stretch.ui.Chip
import dev.chieppa.stretch.ui.ChipKind
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/**
 * The switcher over the dimmed window. Typing searches the strip's sources, the files they change and the active
 * source's other files, and what followed repos hold that the strip does not, in sections; Tab moves through the
 * scopes and Up and Down through the rows. Enter switches to a row, Ctrl Enter opens it in a new pane and Alt Enter
 * opens it without switching from the active source. Before anything is typed it suggests what to open from followed
 * repos. A pasted github.com link or a source ref turns it into a card of what that points at, with the ways it can
 * open; a pasted folder into one row that follows the repo in it.
 */
@Composable
fun Switcher(workbench: Workbench) {
    val switcher = workbench.switcher
    if (!switcher.open) return
    val commands = remember(workbench) { SwitcherCommands(workbench) }
    val view = rememberSwitcherView(workbench, switcher)
    val target = view.target
    SearchPalette(
        query = switcher.query,
        onQueryChange = switcher::type,
        onDismiss = switcher::close,
        placeholder = placeholder(workbench, switcher.scope),
        width = ArcSizes.switcherWidth,
        top = ArcSizes.switcherTop,
        scrim = true,
        icon = if (target?.link == true) StretchIcons.Link else StretchIcons.Search,
        queryStyle = if (target?.link == true) Stretch.type.value.merge(TextStyle(fontSize = Stretch.type.body.fontSize)) else null,
        hints = switcherHints(view),
        onKey = { view.onKey(it, switcher, commands) },
        trailing = {
            if (view.searching) {
                val total = view.sections.sumOf { it.total }
                RowMeta(pluralStringResource(Res.plurals.switcher_results, total, total))
                ScopePicker(switcher.scope, switcher::pick)
            } else if (target != null) {
                Chip(chipText(target), kind = ChipKind.On, icon = sourceIcon(target.named))
            }
        },
    ) {
        SwitcherBody(workbench, view, switcher.query, switcher.index, commands)
    }
}

@Composable
private fun placeholder(workbench: Workbench, scope: SwitcherScope): String {
    val head = workbench.state.active?.let(workbench.state::snapshot)?.revisions?.head
    return when {
        scope == SwitcherScope.Files && head != null -> stringResource(Res.string.switcher_files_placeholder, Revisions.short(Revision(head.value)))
        scope == SwitcherScope.Sources -> stringResource(Res.string.switcher_sources_placeholder)
        else -> stringResource(Res.string.switcher_placeholder)
    }
}

@Composable
private fun chipText(target: PastedTarget): String = stringResource(
    when (target.named) {
        is Source.Commit -> if (target.link) Res.string.switcher_link_commit else Res.string.switcher_ref_commit
        is Source.PullRequest -> if (target.link) Res.string.switcher_link_pull else Res.string.switcher_ref_pull
        is Source.BranchAgainstBase -> if (target.link) Res.string.switcher_link_branch else Res.string.switcher_ref_branch
        is Source.CommitRange -> if (target.link) Res.string.switcher_link_range else Res.string.switcher_ref_range
    },
)

package dev.chieppa.arc.ui.switcher

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.core.pane.Revision
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.switcher_files_placeholder
import dev.chieppa.arc.design.generated.switcher_hint_beside
import dev.chieppa.arc.design.generated.switcher_hint_open
import dev.chieppa.arc.design.generated.switcher_hint_close
import dev.chieppa.arc.design.generated.switcher_hint_new_pane
import dev.chieppa.arc.design.generated.switcher_hint_scope
import dev.chieppa.arc.design.generated.switcher_hint_switch
import dev.chieppa.arc.design.generated.switcher_link_branch
import dev.chieppa.arc.design.generated.switcher_link_commit
import dev.chieppa.arc.design.generated.switcher_link_pull
import dev.chieppa.arc.design.generated.switcher_link_range
import dev.chieppa.arc.design.generated.switcher_open_as
import dev.chieppa.arc.design.generated.switcher_placeholder
import dev.chieppa.arc.design.generated.switcher_ref_branch
import dev.chieppa.arc.design.generated.switcher_ref_commit
import dev.chieppa.arc.design.generated.switcher_ref_pull
import dev.chieppa.arc.design.generated.switcher_ref_range
import dev.chieppa.arc.design.generated.switcher_results
import dev.chieppa.arc.ui.pulls.currentTime
import dev.chieppa.arc.ui.sources.sourceIcon
import dev.chieppa.arc.ui.util.ArcSizes
import dev.chieppa.arc.ui.util.Revisions
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.frame.PaletteHint
import dev.chieppa.stretch.frame.PaletteSection
import dev.chieppa.stretch.frame.SearchPalette
import dev.chieppa.stretch.frame.chord
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchIcons
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.Chip
import dev.chieppa.stretch.ui.ChipKind
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/**
 * The switcher over the dimmed window. Typing searches the strip's sources, the files they change and the active
 * source's other files, and what followed repos hold that the strip does not, in sections; Tab moves through the
 * scopes and Up and Down through the rows. Enter switches to a row, Ctrl Enter opens it in a new pane and Alt Enter
 * opens it without switching from the active source. A pasted github.com link or a source ref turns it into a card of what that points at, with
 * the ways it can open.
 */
@Composable
fun Switcher(workbench: Workbench) {
    val switcher = workbench.switcher
    if (!switcher.open) return
    val state = workbench.state
    val query = switcher.query
    val commands = remember(workbench) { SwitcherCommands(workbench) }
    val now = currentTime() / MILLIS_PER_SECOND
    val target = remember(query, workbench.gitHubRepos, state.repos) { PastedTargets.read(query, workbench.gitHubRepos, state.repos.keys) }
    val inputs = rememberSwitcherInputs(workbench, if (target == null) query else "", switcher.scope)
    val sections = if (target == null) SwitcherSearch.sections(query, switcher.scope, inputs) else emptyList()
    val rows = sections.flatMap { it.items }
    val options = target?.let(OpenAs::of).orEmpty()
    val facts = produceState(LinkFacts(), target) { value = LinkFacts(); target?.let { value = LinkFactsReader.read(workbench.data, it) } }.value
    val count = if (target == null) rows.size else options.size
    SearchPalette(
        query = query,
        onQueryChange = switcher::type,
        onDismiss = switcher::close,
        placeholder = placeholder(workbench, switcher.scope),
        width = ArcSizes.switcherWidth,
        top = ArcSizes.switcherTop,
        scrim = true,
        icon = if (target?.link == true) StretchIcons.Link else StretchIcons.Search,
        queryStyle = if (target?.link == true) Stretch.type.value.merge(TextStyle(fontSize = Stretch.type.body.fontSize)) else null,
        hints = if (target == null) searchHints() else linkHints(),
        onKey = { event ->
            val chord = event.chord()
            val pick = SwitcherKeys.pickOf(event)
            when {
                pick != null && target == null -> rows.getOrNull(switcher.index)?.let { commands.pick(it, pick) } != null
                pick != null -> options.getOrNull(switcher.index)?.let { commands.pick(it, pick) } != null
                chord == SwitcherKeys.DOWN -> { switcher.move(1, count); true }
                chord == SwitcherKeys.UP -> { switcher.move(-1, count); true }
                chord == SwitcherKeys.SCOPE && target == null -> { switcher.pick(switcher.scope.next()); true }
                chord == SwitcherKeys.SCOPE_BACK && target == null -> { switcher.pick(switcher.scope.next(back = true)); true }
                else -> false
            }
        },
        trailing = {
            if (target == null) {
                val total = sections.sumOf { it.total }
                RowMeta(pluralStringResource(Res.plurals.switcher_results, total, total))
                ScopePicker(switcher.scope, switcher::pick)
            } else {
                Chip(chipText(target), kind = ChipKind.On, icon = sourceIcon(target.named))
            }
        },
    ) {
        if (target == null) {
            SearchResults(workbench, sections, query, switcher.index, now) { commands.pick(it, SwitcherPick.Switch) }
        } else {
            Column(Modifier.padding(start = StretchSpacing.space6, end = StretchSpacing.space6, bottom = StretchSpacing.space6)) {
                LinkCard(target, facts, now)
                if (options.isNotEmpty()) PaletteSection(stringResource(Res.string.switcher_open_as))
                options.forEachIndexed { i, option ->
                    val held = option.source in state.workspace.sources
                    OpenAsRow(option, facts, held, i == switcher.index) { commands.pick(option, SwitcherPick.Switch) }
                }
            }
        }
    }
}

@Composable
private fun placeholder(workbench: Workbench, scope: SwitcherScope): String {
    val head = workbench.state.active?.let(workbench.state::snapshot)?.revisions?.head
    return if (scope == SwitcherScope.Files && head != null) {
        stringResource(Res.string.switcher_files_placeholder, Revisions.short(Revision(head.value)))
    } else {
        stringResource(Res.string.switcher_placeholder)
    }
}

@Composable
private fun searchHints() = listOf(
    PaletteHint(SwitcherKeys.caps(SwitcherKeys.SWITCH), stringResource(Res.string.switcher_hint_switch)),
    PaletteHint(SwitcherKeys.caps(SwitcherKeys.NEW_PANE), stringResource(Res.string.switcher_hint_new_pane)),
    PaletteHint(SwitcherKeys.caps(SwitcherKeys.BESIDE), stringResource(Res.string.switcher_hint_beside)),
    PaletteHint(SwitcherKeys.caps(SwitcherKeys.SCOPE), stringResource(Res.string.switcher_hint_scope)),
)

@Composable
private fun linkHints() = listOf(
    PaletteHint(SwitcherKeys.caps(SwitcherKeys.SWITCH), stringResource(Res.string.switcher_hint_open)),
    PaletteHint(SwitcherKeys.caps(SwitcherKeys.NEW_PANE), stringResource(Res.string.switcher_hint_new_pane)),
    PaletteHint(SwitcherKeys.caps(SwitcherKeys.CLOSE), stringResource(Res.string.switcher_hint_close)),
)

@Composable
private fun chipText(target: PastedTarget): String = stringResource(
    when (target.named) {
        is Source.Commit -> if (target.link) Res.string.switcher_link_commit else Res.string.switcher_ref_commit
        is Source.PullRequest -> if (target.link) Res.string.switcher_link_pull else Res.string.switcher_ref_pull
        is Source.BranchAgainstBase -> if (target.link) Res.string.switcher_link_branch else Res.string.switcher_ref_branch
        is Source.CommitRange -> if (target.link) Res.string.switcher_link_range else Res.string.switcher_ref_range
    },
)

private const val MILLIS_PER_SECOND = 1000L

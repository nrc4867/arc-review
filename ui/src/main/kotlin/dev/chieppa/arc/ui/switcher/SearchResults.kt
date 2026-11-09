package dev.chieppa.arc.ui.switcher

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.switcher_files_in
import dev.chieppa.arc.design.generated.switcher_followed
import dev.chieppa.arc.design.generated.switcher_followed_note
import dev.chieppa.arc.design.generated.switcher_in_review
import dev.chieppa.arc.design.generated.switcher_suggested
import dev.chieppa.arc.ui.util.SourceLabels
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.frame.PaletteSection
import dev.chieppa.stretch.theme.StretchSpacing
import org.jetbrains.compose.resources.stringResource

/**
 * The switcher's results for a search: each section under its eyebrow, the row at [index] across them highlighted.
 * Before anything is typed, followed repos' section holds what is suggested.
 * A click picks a row as Enter does.
 */
@Composable
fun SearchResults(workbench: Workbench, sections: List<SwitcherSection>, query: String, index: Int, nowSeconds: Long, onPick: (SwitcherItem) -> Unit) {
    var row = 0
    Column(Modifier.padding(start = StretchSpacing.space6, end = StretchSpacing.space6, top = StretchSpacing.space4, bottom = StretchSpacing.space6)) {
        for (section in sections) {
            when (section.kind) {
                SectionKind.InWorkspace -> PaletteSection(stringResource(Res.string.switcher_in_review))
                SectionKind.Files -> {
                    val refs = section.items.mapNotNull { (it as? SwitcherItem.Changed)?.source ?: (it as? SwitcherItem.AtHead)?.source }.distinct()
                    PaletteSection(stringResource(Res.string.switcher_files_in, refsText(refs.map(SourceLabels::of))))
                }
                SectionKind.Followed -> PaletteSection(
                    stringResource(if (query.isBlank()) Res.string.switcher_suggested else Res.string.switcher_followed),
                    note = stringResource(Res.string.switcher_followed_note),
                )
            }
            for (item in section.items) {
                val highlighted = row++ == index
                val pick = { onPick(item) }
                when (item) {
                    is SwitcherItem.InWorkspace -> InWorkspaceRow(item, workbench.state, query, highlighted, pick)
                    is SwitcherItem.Changed -> FileResultRow(item.source, item.file.path, item.file, query, highlighted, pick)
                    is SwitcherItem.AtHead -> FileResultRow(item.source, item.path, null, query, highlighted, pick)
                    is SwitcherItem.FollowedPull -> FollowedPullRow(item, workbench.watch.viewer, query, highlighted, pick)
                    is SwitcherItem.FollowedBranch -> FollowedBranchRow(item, query, nowSeconds, highlighted, pick)
                    is SwitcherItem.Recent -> RecentRow(item, query, nowSeconds, highlighted, pick)
                }
            }
        }
    }
}

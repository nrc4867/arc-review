package dev.chieppa.arc.ui.status

import dev.chieppa.arc.ui.util.SourceLabels
import androidx.compose.runtime.Composable
import dev.chieppa.arc.core.pane.Page
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.status_commit
import dev.chieppa.arc.design.generated.status_commits
import dev.chieppa.arc.design.generated.status_matches
import dev.chieppa.arc.design.generated.status_caret
import dev.chieppa.arc.design.generated.status_tree_sitter
import dev.chieppa.arc.ui.navigation.IndexStatusItem
import dev.chieppa.arc.ui.runs.RunStatusEnd
import dev.chieppa.arc.ui.runs.RunStatusStart
import dev.chieppa.arc.ui.runs.RunsStatusEnd
import dev.chieppa.arc.ui.runs.RunsStatusStart
import dev.chieppa.arc.ui.navigation.SyntaxTokens
import dev.chieppa.arc.ui.sources.sourceIcon
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.code.CodeLanguages
import dev.chieppa.stretch.frame.StatusBar
import dev.chieppa.stretch.frame.StatusItem
import org.jetbrains.compose.resources.stringResource

/**
 * The status bar: the active source, its commits and head, and what the code index holds; at the end, the focused
 * pane's find matches while it is finding, its caret, its file's language, and tree-sitter when tree-sitter reads it. A saved filter's tab, a run filter's, a run's and a revision matrix's show their own instead.
 */
@Composable
fun ArcStatusBar(workbench: Workbench) {
    val state = workbench.state
    val source = state.active
    val snapshot = source?.let(state::snapshot)
    val pane = workbench.panes.focused
    val filter = (state.paneLayout.activeTab?.let(state::page) as? Page.Filter)?.let { workbench.watch.watchlist.filter(it.filter) }
    if (filter != null) {
        StatusBar(start = { FilterStatusStart(filter, workbench.watch) }, end = { FilterStatusEnd(workbench.watch) })
        return
    }
    val page = state.paneLayout.activeTab?.let(state::page)
    val runWatch = workbench.runs.watch
    if (page is Page.Runs) {
        val runs = runWatch.watchlist.filter(page.filter)
        StatusBar(start = { runs?.let { RunsStatusStart(it, runWatch) } }, end = { RunsStatusEnd(runWatch, runs) })
        return
    }
    val run = (page as? Page.Run)?.let { runWatch.run(it.run) }
    if (run != null) {
        StatusBar(start = { RunStatusStart(run, runWatch) }, end = { RunStatusEnd(run, runWatch) })
        return
    }
    val matrix = page as? Page.RevisionMatrix
    if (matrix != null) {
        StatusBar(start = { RevisionStatusStart(matrix.source, workbench) }, end = { RevisionStatusEnd(matrix.source, workbench) })
        return
    }
    StatusBar(
        start = {
            if (source != null) StatusItem(SourceLabels.of(source), sourceIcon(source))
            if (snapshot != null) {
                val head = snapshot.revisions.head.short()
                val count = snapshot.commits.size
                StatusItem(if (count == 1) stringResource(Res.string.status_commit, head) else stringResource(Res.string.status_commits, count, head))
            }
            IndexStatusItem(workbench.navigation)
        },
        end = {
            val find = pane?.let { workbench.paneCommands[it.id.value]?.find }
            if (find != null && find.open && find.matches.isNotEmpty()) {
                StatusItem(stringResource(Res.string.status_matches, find.current + 1, find.matches.size))
            }
            workbench.navigation.caret?.takeIf { it.paneId == pane?.id?.value }?.let { StatusItem(stringResource(Res.string.status_caret, it.line, it.column + 1)) }
            pane?.let { CodeLanguages.forPath(it.path) }?.let { StatusItem(LanguageNames.of(it)) }
            if (pane != null && SyntaxTokens.reads(pane.path)) StatusItem(stringResource(Res.string.status_tree_sitter))
        },
    )
}

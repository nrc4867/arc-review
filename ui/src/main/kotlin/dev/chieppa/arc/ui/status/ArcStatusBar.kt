package dev.chieppa.arc.ui.status

import dev.chieppa.arc.ui.util.SourceLabels
import androidx.compose.runtime.Composable
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.status_commit
import dev.chieppa.arc.design.generated.status_commits
import dev.chieppa.arc.design.generated.status_matches
import dev.chieppa.arc.ui.sources.sourceIcon
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.code.CodeLanguages
import dev.chieppa.stretch.frame.StatusBar
import dev.chieppa.stretch.frame.StatusItem
import org.jetbrains.compose.resources.stringResource

/**
 * The status bar: the active source, its commits and head; at the end, the focused pane's find matches while it is
 * finding, and its file's language.
 */
@Composable
fun ArcStatusBar(workbench: Workbench) {
    val state = workbench.state
    val source = state.active
    val snapshot = source?.let(state::snapshot)
    val pane = workbench.panes.focused
    StatusBar(
        start = {
            if (source != null) StatusItem(SourceLabels.of(source), sourceIcon(source))
            if (snapshot != null) {
                val head = snapshot.revisions.head.short()
                val count = snapshot.commits.size
                StatusItem(if (count == 1) stringResource(Res.string.status_commit, head) else stringResource(Res.string.status_commits, count, head))
            }
        },
        end = {
            val find = pane?.let { workbench.paneCommands[it.id.value]?.find }
            if (find != null && find.open && find.matches.isNotEmpty()) {
                StatusItem(stringResource(Res.string.status_matches, find.current + 1, find.matches.size))
            }
            pane?.let { CodeLanguages.forPath(it.path) }?.let { StatusItem(LanguageNames.of(it)) }
        },
    )
}

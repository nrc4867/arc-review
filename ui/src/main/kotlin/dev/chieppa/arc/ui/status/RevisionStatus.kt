package dev.chieppa.arc.ui.status

import androidx.compose.runtime.Composable
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.revisions_number
import dev.chieppa.arc.design.generated.revisions_revisions
import dev.chieppa.arc.design.generated.status_commits_picked
import dev.chieppa.arc.design.generated.status_pinned
import dev.chieppa.arc.ui.sources.sourceIcon
import dev.chieppa.arc.ui.util.SourceLabels
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.frame.StatusItem
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/** The status bar's start for a revision matrix's tab: its source, how many revisions it has, and which are pinned. */
@Composable
fun RevisionStatusStart(source: Source, workbench: Workbench) {
    StatusItem(SourceLabels.of(source), sourceIcon(source))
    val revisions = workbench.state.revisions[source]?.history?.revisions ?: return
    StatusItem(pluralStringResource(Res.plurals.revisions_revisions, revisions.size, revisions.size))
    val pinned = revisions.filter { it.pinned }
    if (pinned.isNotEmpty()) {
        val numbers = pinned.map { stringResource(Res.string.revisions_number, it.number) }
        StatusItem(stringResource(Res.string.status_pinned, numbers.joinToString(" ")))
    }
}

/** The status bar's end for a revision matrix's tab: how many commits are picked in the Commits drawer, when any are. */
@Composable
fun RevisionStatusEnd(source: Source, workbench: Workbench) {
    val picked = workbench.revisions.picks[source].orEmpty().size
    if (picked > 0) StatusItem(pluralStringResource(Res.plurals.status_commits_picked, picked, picked))
}

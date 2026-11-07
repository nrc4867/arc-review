package dev.chieppa.arc.ui.revisions

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import dev.chieppa.arc.core.revision.RevisionHistory
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.revisions_commits
import dev.chieppa.arc.design.generated.revisions_force_pushes
import dev.chieppa.arc.design.generated.revisions_pushes
import dev.chieppa.arc.design.generated.revisions_rebases
import dev.chieppa.arc.design.generated.revisions_revisions
import dev.chieppa.arc.design.generated.revisions_separator
import dev.chieppa.arc.ui.util.bottomRule
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchSpacing
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/**
 * The Commits drawer, opened from the rail's Revisions button, for the active source: how many commits, pushes and
 * rebases it has had, every commit with the revision that brought it, each one a box to pick for comparing, and once
 * two are picked, what differs between them and the button that compares them. Opening it opens the source's revision
 * matrix too. With no source, or before its revisions are read, it is plain.
 */
@Composable
fun CommitsDrawer(workbench: Workbench) {
    val state = workbench.state
    val source = state.active ?: return
    val head = state.snapshot(source)?.revisions?.head
    LaunchedEffect(source) { workbench.panes.openRevisions(source) }
    LaunchedEffect(source, head) { if (head != null) workbench.revisions.load(source) }
    val history = state.revisions[source]?.history ?: return
    val c = Stretch.colors
    val picks = workbench.revisions.picks[source].orEmpty()
    Column(Modifier.fillMaxWidth()) {
        BasicText(
            summary(source, history),
            Modifier.fillMaxWidth().bottomRule(c.linePanel)
                .padding(start = StretchSpacing.space14, end = StretchSpacing.space14, top = StretchSpacing.space10, bottom = StretchSpacing.space12),
            style = Stretch.type.bodySm.merge(TextStyle(color = c.inkMuted)),
        )
        Column(Modifier.fillMaxWidth().padding(StretchSpacing.space6)) {
            for ((commit, revision) in history.commits) {
                CommitRow(commit, revision.number, commit.id in picks) { workbench.revisions.togglePick(source, commit.id) }
            }
        }
        workbench.revisions.comparison(source)?.let { comparison ->
            CompareSection(comparison, workbench.revisions.differing[comparison.key]) { workbench.revisions.compare(source) }
        }
    }
}

/** `6 commits · 5 pushes · 1 rebase`: the source's commits, its revisions (pushes, for a pull request) and its rewrites. */
@Composable
private fun summary(source: Source, history: RevisionHistory): String {
    val commits = history.commits.size
    val revisions = history.revisions.size
    val rebases = history.revisions.count { it.rebased }
    val forced = history.rewrites - rebases
    val parts = listOfNotNull(
        pluralStringResource(Res.plurals.revisions_commits, commits, commits),
        if (source is Source.PullRequest) pluralStringResource(Res.plurals.revisions_pushes, revisions, revisions)
        else pluralStringResource(Res.plurals.revisions_revisions, revisions, revisions),
        if (rebases > 0) pluralStringResource(Res.plurals.revisions_rebases, rebases, rebases) else null,
        if (forced > 0) pluralStringResource(Res.plurals.revisions_force_pushes, forced, forced) else null,
    )
    return parts.joinToString(stringResource(Res.string.revisions_separator))
}

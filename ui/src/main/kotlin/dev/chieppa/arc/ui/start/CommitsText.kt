package dev.chieppa.arc.ui.start

import androidx.compose.runtime.Composable
import dev.chieppa.arc.core.change.SourceSnapshot
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.start_commits
import dev.chieppa.arc.design.generated.start_commits_since
import dev.chieppa.arc.ui.review.WorkspaceState
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/**
 * How many commits [snapshot] of [source] has, and how many came after the newest head the person reviewed a file at,
 * when that head is one of them and commits came after it.
 */
@Composable
fun commitsText(state: WorkspaceState, source: Source, snapshot: SourceSnapshot): String {
    val count = snapshot.commits.size
    val commits = pluralStringResource(Res.plurals.start_commits, count, count)
    val reviewed = state.workspace.marksIn(source).values.mapNotNull { it.reviewed?.head }.toSet()
    val at = snapshot.commits.indexOfLast { it.id in reviewed }
    val since = if (at < 0) 0 else count - 1 - at
    return if (since == 0) commits else stringResource(Res.string.start_commits_since, commits, since)
}

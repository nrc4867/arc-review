package dev.chieppa.arc.ui.files

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import dev.chieppa.arc.core.mark.FileReviewState
import dev.chieppa.arc.design.ArcIcons
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.files_changed_since_count
import dev.chieppa.arc.design.generated.files_reviewed_count
import dev.chieppa.arc.design.generated.files_set_aside_count
import dev.chieppa.arc.design.generated.files_since_review
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchSizes
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.ToggleButton
import org.jetbrains.compose.resources.stringResource

/**
 * The head of the Files drawer for the active source: one compact row with what the tree shows (all changes or one
 * commit) and the changed-since filter, over how far reviewing has got.
 */
@Composable
fun FilesHeader(workbench: Workbench, modifier: Modifier = Modifier) {
    val state = workbench.state
    val source = state.active ?: return
    val snapshot = state.snapshot(source)
    val c = Stretch.colors
    Column(
        modifier.fillMaxWidth().padding(horizontal = StretchSpacing.space8, vertical = StretchSpacing.space6),
        verticalArrangement = Arrangement.spacedBy(StretchSpacing.space4),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space6), verticalAlignment = Alignment.CenterVertically) {
            val commits = snapshot?.commits.orEmpty()
            val scoped = state.viewOf(source).commit
            ScopeMenu(commits, commits.firstOrNull { it.id == scoped }, { workbench.sources.setScope(source, it?.id) }, Modifier.weight(1f))
            ToggleButton(
                stringResource(Res.string.files_since_review),
                workbench.sinceReview,
                { workbench.sinceReview = it },
                icon = ArcIcons.ChangedSince,
                height = StretchSizes.seg,
            )
        }
        val states = state.states(source).values
        val progress = state.progress(source)
        if (progress != null) {
            val small = Stretch.type.bodySm
            Row(horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space8)) {
                BasicText(
                    stringResource(Res.string.files_reviewed_count, progress.reviewed, progress.total),
                    style = small.merge(TextStyle(color = c.ink, fontWeight = FontWeight.SemiBold)),
                )
                val changed = states.count { it == FileReviewState.ChangedSinceReviewed }
                if (changed > 0) BasicText(stringResource(Res.string.files_changed_since_count, changed), style = small.merge(TextStyle(color = c.warnInk)))
                val aside = states.count { it == FileReviewState.SetAside }
                if (aside > 0) BasicText(stringResource(Res.string.files_set_aside_count, aside), style = small.merge(TextStyle(color = c.inkMuted)))
            }
        }
    }
}

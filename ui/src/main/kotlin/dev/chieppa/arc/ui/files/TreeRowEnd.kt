package dev.chieppa.arc.ui.files

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.TextStyle
import dev.chieppa.arc.core.mark.FileReviewState
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.files_reviewed
import dev.chieppa.arc.design.generated.sources_progress
import dev.chieppa.arc.ui.sources.ProgressRing
import dev.chieppa.arc.ui.tree.FileTreeItem
import dev.chieppa.arc.ui.tree.LineCounts
import dev.chieppa.arc.ui.tree.ReviewTree
import dev.chieppa.arc.ui.workbench.ReviewTargets
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchSpacing
import org.jetbrains.compose.resources.stringResource

/**
 * The end of a tree row: a source's progress; a folder's file count and mark for all its files; a file's line counts
 * and mark; the set-aside fold's count.
 */
@Composable
fun TreeRowEnd(item: FileTreeItem, workbench: Workbench) {
    val c = Stretch.colors
    val mono = Stretch.type.valueSm
    val reviewed = stringResource(Res.string.files_reviewed)
    Row(horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space6), verticalAlignment = Alignment.CenterVertically) {
        when (item) {
            is FileTreeItem.SourceRow -> item.progress?.let { progress ->
                ProgressRing(if (progress.total == 0) 1f else progress.reviewed.toFloat() / progress.total)
                BasicText(stringResource(Res.string.sources_progress, progress.reviewed, progress.total), style = mono.merge(TextStyle(color = c.inkMuted)))
            }
            is FileTreeItem.FolderRow -> {
                BasicText(item.files.toString(), style = mono.merge(TextStyle(color = c.inkSubtle)))
                MarkBox(item.marked, { on -> workbench.marks.setReviewed(item.source, ReviewTargets.selected(ReviewTree.roots(workbench.state), setOf(item.key))[item.source].orEmpty(), on) }, reviewed)
            }
            is FileTreeItem.FileRow -> {
                val counts = LineCounts.of(item.file)
                counts.added?.let { BasicText(it, style = mono.merge(TextStyle(color = c.diffAddStrong))) }
                counts.removed?.let { BasicText(it, style = mono.merge(TextStyle(color = c.diffDelStrong))) }
                if (item.state != FileReviewState.SetAside) {
                    MarkBox(
                        checked = item.state == FileReviewState.Reviewed,
                        onToggle = { on -> workbench.marks.setReviewed(item.source, listOf(item.file.path), on) },
                        description = reviewed,
                        changedSince = item.state == FileReviewState.ChangedSinceReviewed,
                    )
                }
            }
            is FileTreeItem.AsideRow -> BasicText(item.files.toString(), style = mono.merge(TextStyle(color = c.inkSubtle)))
        }
    }
}

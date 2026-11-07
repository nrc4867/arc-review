package dev.chieppa.arc.ui.conversation

import androidx.compose.runtime.Composable
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.ui.Divider

/** The Staged comments drawer: one row per staged comment across the workspace's sources. Empty while none is staged. */
@Composable
fun StagedCommentsContent(workbench: Workbench) {
    for (comment in stagedComments(workbench)) {
        StagedRow(comment)
        Divider()
    }
}

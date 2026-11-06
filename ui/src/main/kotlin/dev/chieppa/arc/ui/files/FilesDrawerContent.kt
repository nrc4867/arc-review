package dev.chieppa.arc.ui.files

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.ui.Divider

/** The Files drawer: the head for the active source over the tree of every source's files. Empty while there are none. */
@Composable
fun ColumnScope.FilesDrawerContent(workbench: Workbench) {
    if (workbench.state.review.sources.isEmpty()) return
    FilesHeader(workbench)
    Divider()
    FileTree(workbench, Modifier.weight(1f).fillMaxWidth())
}

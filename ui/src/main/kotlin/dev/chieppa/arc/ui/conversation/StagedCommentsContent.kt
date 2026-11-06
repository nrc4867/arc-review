package dev.chieppa.arc.ui.conversation

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.ui.Divider

/** The Staged comments drawer: one row per staged comment across the review's sources. Empty while none is staged. */
@Composable
fun ColumnScope.StagedCommentsContent(workbench: Workbench) {
    val staged = stagedComments(workbench)
    if (staged.isEmpty()) return
    LazyColumn(Modifier.weight(1f).fillMaxWidth()) {
        items(staged, key = { it.id }) { comment ->
            StagedRow(comment)
            Divider()
        }
    }
}

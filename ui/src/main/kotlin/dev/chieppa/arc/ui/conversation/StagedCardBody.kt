package dev.chieppa.arc.ui.conversation

import androidx.compose.runtime.Composable
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.ui.Divider

/**
 * The staged batch for [source] as a card on its home, under the card's header: the verdict and summary, one row per
 * staged comment, and the push at the foot, as the Staged comments drawer has them.
 */
@Composable
fun StagedCardBody(workbench: Workbench, source: Source) {
    val staged = workbench.comments.pusher.staged(source)
    StagedHead(workbench, source, heading = false)
    for (comment in staged) {
        Divider()
        StagedRow(comment, workbench)
    }
    Divider()
    StagedFooter(workbench, source, staged)
}

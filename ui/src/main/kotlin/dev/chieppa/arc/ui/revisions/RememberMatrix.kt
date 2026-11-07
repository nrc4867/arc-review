package dev.chieppa.arc.ui.revisions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import dev.chieppa.arc.core.revision.RevisionMatrices
import dev.chieppa.arc.core.revision.RevisionMatrix
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.ui.workbench.Workbench

/**
 * The revision matrix of [source] from its revisions as [read], its marks and where its files stand, and the threads
 * the conversation places on its revisions. Worked out again only when one of those changes.
 */
@Composable
fun rememberMatrix(workbench: Workbench, source: Source, read: RevisionsRead): RevisionMatrix {
    val state = workbench.state
    val marks = state.workspace.marksIn(source)
    val states = state.states(source)
    val history = read.history
    val threads = remember(history) {
        val paths = history.revisions.flatMap { it.fingerprints.keys }.distinct()
        workbench.conversation.revisionThreads(source, paths, history.revisions.map { it.head })
    }
    return remember(history, marks, states, threads) { RevisionMatrices.of(history, marks, states, threads) }
}

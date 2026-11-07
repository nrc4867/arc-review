package dev.chieppa.arc.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performMouseInput
import androidx.compose.ui.test.rightClick
import androidx.compose.ui.test.v2.runComposeUiTest
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.host.github.pulls.PullState
import dev.chieppa.arc.ui.actions.ArcActionIds
import dev.chieppa.arc.ui.tree.FileTreeItem
import dev.chieppa.arc.ui.tree.ReviewTree
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class EndedPullsUiTest {
    private val data = FakeReviewData()
    private val merged = Source.PullRequest(data.repo, 1)
    private val closed = Source.PullRequest(data.repo, 2)
    private val open = Source.PullRequest(data.repo, 3)

    init {
        data.pulls[merged] = FakePulls.pull(1, "Merged one").copy(state = PullState.Merged)
        data.pulls[closed] = FakePulls.pull(2, "Closed one").copy(state = PullState.Closed)
        data.pulls[open] = FakePulls.pull(3, "Open one")
    }

    private fun androidx.compose.ui.test.ComposeUiTest.openPulls() = openReview(data).also { workbench ->
        listOf(merged, closed, open).forEach { assertTrue(workbench.addSource(it.ref)) }
        waitUntil { listOf(merged, closed, open).all { it in workbench.state.pulls } }
        waitForIdle()
    }

    @Test
    fun mergedAndClosedPullRequestsAreMarkedEndedInTheTree() = runComposeUiTest {
        val workbench = openPulls()
        val ended = ReviewTree.roots(workbench.state).map { it.item }.filterIsInstance<FileTreeItem.SourceRow>().associate { it.source to it.ended }
        assertEquals(mapOf(data.source to false, merged to true, closed to true, open to false), ended)
        assertEquals(listOf(merged, closed), workbench.endedPulls)
    }

    @Test
    fun oneActionClosesEveryMergedAndClosedPullRequestAndKeepsTheRest() = runComposeUiTest {
        val workbench = openPulls()
        val action = workbench.registry[ArcActionIds.CLOSE_ENDED_PULLS]!!
        assertTrue(action.isAvailable())
        runOnIdle { action.run() }
        waitForIdle()
        assertEquals(listOf(data.source, open), workbench.state.workspace.sources)
        assertFalse(action.isAvailable())
    }

    @Test
    fun aChipsMenuOffersClosingThemWhileThereAreAny() = runComposeUiTest {
        val workbench = openPulls()
        onNodeWithContentDescription(open.ref).performMouseInput { rightClick() }
        waitForIdle()
        onNodeWithText("Close merged and closed pull requests").performClick()
        waitForIdle()
        assertEquals(listOf(data.source, open), workbench.state.workspace.sources)
        onNodeWithContentDescription(open.ref).performMouseInput { rightClick() }
        waitForIdle()
        assertFalse(shows("Close merged and closed pull requests"))
    }
}

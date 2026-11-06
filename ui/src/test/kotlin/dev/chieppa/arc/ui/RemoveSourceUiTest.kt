package dev.chieppa.arc.ui

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performMouseInput
import androidx.compose.ui.test.rightClick
import androidx.compose.ui.test.v2.runComposeUiTest
import dev.chieppa.arc.core.mark.FileReviewState
import dev.chieppa.arc.core.pane.Revision
import dev.chieppa.arc.ui.actions.ArcActionIds
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class RemoveSourceUiTest {
    private val data = FakeReviewData()

    @Test
    fun theChipsRemoveButtonTakesTheSourceOutKeepingItsMarksForWhenItIsAddedAgain() = runComposeUiTest {
        val workbench = openReview(data)
        workbench.marks.setReviewed(data.source, listOf("app/Main.kt"), true)
        workbench.panes.openChange(data.source, "app/util/Text.kt")
        val pinned = workbench.panes.openFile(data.repo, Revision(data.head.value), "docs/Guide.md", data.source, pinned = true)
        waitForIdle()
        onNodeWithContentDescription("Remove source").performClick()
        waitForIdle()

        assertTrue(workbench.state.review.sources.isEmpty())
        assertEquals(listOf(pinned), workbench.state.review.panes.map { it.id })
        assertTrue(pinned.value in workbench.paneLayout.tabs)
        assertEquals(1, workbench.state.review.marks.size)

        assertTrue(workbench.addSource(data.source.ref))
        waitUntil { workbench.state.snapshot(data.source) != null }
        assertEquals(FileReviewState.Reviewed, workbench.state.states(data.source)["app/Main.kt"])
        assertTrue(pinned.value in workbench.paneLayout.tabs)
    }

    @Test
    fun theChipsMenuAndThePaletteRemoveTheSource() = runComposeUiTest {
        val workbench = openReview(data)
        onNodeWithContentDescription(data.source.ref).performMouseInput { rightClick() }
        waitForIdle()
        assertTrue(shows("Copy source ref"))
        onNodeWithText("Remove source").performClick()
        waitForIdle()
        assertTrue(workbench.state.review.sources.isEmpty())

        workbench.addSource(data.source.ref)
        waitForIdle()
        workbench.registry[ArcActionIds.REMOVE_SOURCE]!!.run()
        waitForIdle()
        assertNull(workbench.state.active)
        assertTrue(onAllNodesWithContentDescription(data.source.ref).fetchSemanticsNodes().isEmpty())
    }

    @Test
    fun copySourceRefCopiesTheFullRef() = runComposeUiTest {
        val copied = CopiedText()
        openReview(data, copied)
        onAllNodesWithContentDescription(data.source.ref).onFirst().performMouseInput { rightClick() }
        waitForIdle()
        onNodeWithText("Copy source ref").performClick()
        assertEquals(data.source.ref, copied.last)
    }

    @Test
    fun hoveringAChipShowsItsRemoveButtonEvenAfterTheReviewSelectorWasOpened() = runComposeUiTest {
        val workbench = openReview(data)
        val other = "demo@" + "c".repeat(40)
        assertTrue(workbench.addSource(other))
        workbench.activateSource(data.source)
        waitForIdle()
        val enabledRemoves = { onAllNodesWithContentDescription("Remove source").fetchSemanticsNodes().count { SemanticsProperties.Disabled !in it.config } }
        assertEquals(1, enabledRemoves())
        onAllNodesWithContentDescription("Review").onFirst().performClick()
        waitForIdle()
        assertEquals(2, onAllNodes(isRoot()).fetchSemanticsNodes().size)
        onAllNodes(isRoot()).onLast().performKeyInput { pressKey(Key.Escape) }
        waitForIdle()
        assertEquals(1, onAllNodes(isRoot()).fetchSemanticsNodes().size)
        onNodeWithContentDescription(other).performMouseInput { moveTo(center) }
        waitForIdle()
        assertEquals(2, enabledRemoves())
    }
}

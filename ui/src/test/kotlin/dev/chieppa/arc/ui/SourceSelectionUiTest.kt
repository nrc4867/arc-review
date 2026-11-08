package dev.chieppa.arc.ui

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.click
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performMouseInput
import androidx.compose.ui.test.v2.runComposeUiTest
import dev.chieppa.arc.core.source.SourceRefs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class SourceSelectionUiTest {
    private val data = FakeReviewData()
    private val otherRef = "demo@" + "c".repeat(40)
    private val other = SourceRefs.parse(otherRef)!!

    private fun ComposeUiTest.ctrlClick(ref: String) {
        onRoot().performKeyInput { keyDown(shortcutKey) }
        onNodeWithContentDescription(ref).performMouseInput { click() }
        onRoot().performKeyInput { keyUp(shortcutKey) }
        waitForIdle()
    }

    @Test
    fun ctrlClickAddsASourceToTheSelectionAndTakesItOutAgain() = runComposeUiTest {
        val workbench = openReview(data)
        assertTrue(workbench.addSourceBeside(otherRef))
        waitForIdle()
        assertEquals(listOf(data.source), workbench.state.selection)
        onNodeWithContentDescription(otherRef).assertIsNotSelected()

        ctrlClick(otherRef)
        assertEquals(data.source, workbench.state.active)
        assertEquals(listOf(data.source, other), workbench.state.selection)
        onNodeWithContentDescription(otherRef).assertIsSelected()
        onNodeWithContentDescription(data.source.ref).assertIsSelected()

        ctrlClick(otherRef)
        assertEquals(listOf(data.source), workbench.state.selection)
        onNodeWithContentDescription(otherRef).assertIsNotSelected()
    }

    @Test
    fun theActiveSourceStaysSelectedAndAPlainClickSelectsOnlyTheSourceClicked() = runComposeUiTest {
        val workbench = openReview(data)
        assertTrue(workbench.addSourceBeside(otherRef))
        waitForIdle()
        ctrlClick(otherRef)
        ctrlClick(data.source.ref)
        assertEquals(listOf(data.source, other), workbench.state.selection)

        onNodeWithContentDescription(otherRef).performMouseInput { click() }
        waitForIdle()
        assertEquals(other, workbench.state.active)
        assertEquals(listOf(other), workbench.state.selection)
        onNodeWithContentDescription(data.source.ref).assertIsNotSelected()
    }

    @Test
    fun aRemovedSourceLeavesTheSelection() = runComposeUiTest {
        val workbench = openReview(data)
        assertTrue(workbench.addSourceBeside(otherRef))
        waitForIdle()
        ctrlClick(otherRef)
        workbench.removeSource(other)
        assertTrue(workbench.addSourceBeside(otherRef))
        waitForIdle()
        assertEquals(listOf(data.source), workbench.state.selection)
    }
}

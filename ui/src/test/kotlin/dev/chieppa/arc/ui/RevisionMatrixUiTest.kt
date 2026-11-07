package dev.chieppa.arc.ui

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.v2.runComposeUiTest
import dev.chieppa.arc.core.pane.Page
import dev.chieppa.arc.ui.drawers.DrawerIds
import dev.chieppa.arc.ui.workbench.Workbench
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class RevisionMatrixUiTest {
    private val data = FakeReviewData()

    private fun androidx.compose.ui.test.ComposeUiTest.openMatrix(): Workbench {
        val workbench = openReview(data)
        workbench.openRevisions(data.source)
        waitUntil { workbench.state.revisions[data.source] != null }
        waitForIdle()
        return workbench
    }

    private fun androidx.compose.ui.test.ComposeUiTest.cells(label: String) =
        onAllNodesWithContentDescription(label).fetchSemanticsNodes().size

    @Test
    fun theMatrixShowsWhichFileChangedInWhichRevision() = runComposeUiTest {
        val workbench = openMatrix()
        assertTrue(workbench.state.workspace.pages.single() is Page.RevisionMatrix)
        assertTrue(shows("Revisions · "))
        assertTrue(shows("3 files"))
        assertTrue(shows("2 revisions"))
        assertTrue(shows("pinned: r1 r2"))
        assertEquals(3 + LEGEND, cells("Changed, not reviewed"))
        assertEquals(3, cells("Unchanged"))
        assertTrue(shows("Text.kt @ r2"))
        assertTrue(shows("changed in r2"))
        waitUntil { workbench.state.workspace.headsOf(data.source).size == 1 }
    }

    @Test
    fun aReviewedFileShowsWhereItWasReviewed() = runComposeUiTest {
        val workbench = openMatrix()
        workbench.marks.setReviewed(data.source, listOf("app/util/Text.kt"), true)
        waitForIdle()
        assertEquals(1 + LEGEND, cells("Changed, reviewed"))
        assertEquals(2 + LEGEND, cells("Changed, not reviewed"))
    }

    @Test
    fun arrowsMoveTheSelectionAndEnterOpensTheFileAtThatRevision() = runComposeUiTest {
        val workbench = openMatrix()
        onAllNodesWithContentDescription("Changed, not reviewed").onFirst().performClick()
        waitForIdle()
        onAllNodesWithText("Selected").onFirst().performKeyInput { pressKey(Key.DirectionDown) }
        waitForIdle()
        assertTrue(shows("Main.kt @ r2"))
        assertTrue(shows("unchanged since r1"))
        onAllNodesWithText("Selected").onFirst().performKeyInput { pressKey(Key.Enter) }
        waitForIdle()
        val pane = workbench.panes.focused!!
        assertEquals("app/Main.kt", pane.path)
        assertEquals(data.head.value, pane.revision.value)
        assertEquals(false, pane.hasChange)
    }

    @Test
    fun openDiffShowsTheFilesChangeBetweenTwoRevisions() = runComposeUiTest {
        val workbench = openMatrix()
        onNodeWithText("Open diff base → r2").performClick()
        waitForIdle()
        val pane = workbench.panes.focused!!
        assertEquals("app/util/Text.kt", pane.path)
        assertEquals(data.base.value, pane.base?.value)
    }

    @Test
    fun twoPickedCommitsCompareAsTheFilesWhoseChangeDiffers() = runComposeUiTest {
        val workbench = openMatrix()
        workbench.toggleDrawer(DrawerIds.REVISIONS)
        waitForIdle()
        assertTrue(shows("2 commits · 2 revisions"))
        onAllNodesWithContentDescription("Pick for compare").fetchSemanticsNodes().let { assertEquals(2, it.size) }
        onAllNodesWithContentDescription("Pick for compare").onFirst().performClick()
        onAllNodesWithContentDescription("Pick for compare")[1].performClick()
        waitUntil { shows("files differ") }
        assertTrue(shows("2 files differ"))
        assertTrue(shows("2 commits picked"))
        assertEquals(data.firstHead, data.compared?.first?.head)
        onNodeWithText("Compare").performClick()
        waitForIdle()
        val diffs = workbench.state.workspace.panes.filter { it.base?.value == data.firstHead.value }
        assertEquals(setOf("app/util/Text.kt", "docs/Guide.md"), diffs.map { it.path }.toSet())
        assertEquals("app/util/Text.kt", workbench.panes.focused?.path)
    }

    private companion object {
        /** The legend draws one cell of each kind. */
        const val LEGEND = 1
    }
}

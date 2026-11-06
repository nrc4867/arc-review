package dev.chieppa.arc.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performMouseInput
import androidx.compose.ui.test.rightClick
import androidx.compose.ui.test.v2.runComposeUiTest
import dev.chieppa.arc.core.mark.FileReviewState
import dev.chieppa.arc.core.pane.PaneView
import dev.chieppa.arc.ui.drawers.DrawerIds
import dev.chieppa.arc.ui.tree.TreeKeys
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class ContextMenuUiTest {
    private val data = FakeReviewData()

    private fun androidx.compose.ui.test.ComposeUiTest.menuOn(text: String) {
        onNodeWithText(text).performMouseInput { rightClick() }
        waitForIdle()
    }

    @Test
    fun aFileRowsMenuOffersEveryWayToOpenItAndRightClickingOpensNothing() = runComposeUiTest {
        val workbench = openReview(data)
        menuOn("Main.kt")
        for (item in listOf("Open", "Open in new tab", "Open to the side", "Open pinned", "Open full file", "Mark reviewed", "Set aside", "Copy path", "Copy ref (repo@sha:path)")) {
            assertTrue(shows(item), item)
        }
        assertFalse(shows("Open in new window"))
        assertNull(workbench.panes.focused)
    }

    @Test
    fun openToTheSideSplitsThePanes() = runComposeUiTest {
        val workbench = openReview(data)
        workbench.panes.openChange(data.source, "app/util/Text.kt")
        waitForIdle()
        menuOn("Main.kt")
        onNodeWithText("Open to the side").performClick()
        waitForIdle()
        assertTrue(workbench.paneLayout.root is dev.chieppa.stretch.frame.PaneSplit)
        assertEquals("app/Main.kt", workbench.panes.focused?.path)
    }

    @Test
    fun openPinnedAndOpenFullFileOpenTheFileThatWay() = runComposeUiTest {
        val workbench = openReview(data)
        menuOn("Main.kt")
        onNodeWithText("Open pinned").performClick()
        waitForIdle()
        assertTrue(workbench.panes.focused!!.pinned)
        menuOn("Text.kt")
        onNodeWithText("Open full file").performClick()
        waitForIdle()
        assertEquals("app/util/Text.kt", workbench.panes.focused?.path)
        assertEquals(PaneView.FullFile, workbench.panes.focused?.view)
    }

    @Test
    fun markingSettingAsideAndRestoringFromTheMenu() = runComposeUiTest {
        val workbench = openReview(data)
        menuOn("Main.kt")
        onNodeWithText("Mark reviewed").performClick()
        waitForIdle()
        assertEquals(FileReviewState.Reviewed, workbench.state.states(data.source)["app/Main.kt"])
        menuOn("Main.kt")
        assertTrue(shows("Unmark reviewed"))
        onNodeWithText("Set aside").performClick()
        waitForIdle()
        assertEquals(FileReviewState.SetAside, workbench.state.states(data.source)["app/Main.kt"])
        workbench.treeState.expanded += TreeKeys.aside(data.source)
        waitForIdle()
        menuOn("Main.kt")
        onNodeWithText("Restore").performClick()
        waitForIdle()
        assertEquals(FileReviewState.NotReviewed, workbench.state.states(data.source)["app/Main.kt"])
    }

    @Test
    fun aFolderRowsMenuMarksEverythingUnderItAndFoldsIt() = runComposeUiTest {
        val workbench = openReview(data)
        menuOn("app")
        assertTrue(shows("Set aside folder"))
        onNodeWithText("Mark all reviewed").performClick()
        waitForIdle()
        val states = workbench.state.states(data.source)
        assertEquals(FileReviewState.Reviewed, states["app/Main.kt"])
        assertEquals(FileReviewState.Reviewed, states["app/util/Text.kt"])
        menuOn("app")
        onNodeWithText("Collapse all").performClick()
        waitForIdle()
        assertFalse(TreeKeys.folder(data.source, "app") in workbench.treeState.expanded)
        menuOn("app")
        onNodeWithText("Expand all").performClick()
        waitForIdle()
        assertTrue(TreeKeys.folder(data.source, "app/util") in workbench.treeState.expanded)
    }

    @Test
    fun copyPathAndRefCopyTheFile() = runComposeUiTest {
        val copied = CopiedText()
        openReview(data, copied)
        menuOn("Main.kt")
        onNodeWithText("Copy path").performClick()
        assertEquals("app/Main.kt", copied.last)
        menuOn("Main.kt")
        onNodeWithText("Copy ref (repo@sha:path)").performClick()
        assertEquals("demo@${data.head.value}:app/Main.kt", copied.last)
    }

    @Test
    fun aTabsMenuPinsAndUnpinsItsPane() = runComposeUiTest {
        val workbench = openReview(data)
        workbench.panes.openChange(data.source, "app/Main.kt")
        workbench.toggleDrawer(DrawerIds.FILES)
        waitForIdle()
        menuOn("Main.kt")
        onNodeWithText("Pin").performClick()
        waitForIdle()
        assertTrue(workbench.panes.focused!!.pinned)
        menuOn("Main.kt")
        onNodeWithText("Unpin").performClick()
        waitForIdle()
        assertFalse(workbench.panes.focused!!.pinned)
    }
}

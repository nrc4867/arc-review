package dev.chieppa.arc.ui

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.test.withKeyDown
import dev.chieppa.arc.core.mark.FileReviewState
import dev.chieppa.arc.core.pane.PaneView
import dev.chieppa.arc.ui.frame.ArcFrame
import dev.chieppa.arc.ui.review.ReviewState
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.test.setStretchContent
import dev.chieppa.stretch.theme.StretchThemes
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class ReviewFlowUiTest {
    private val data = FakeReviewData()

    private fun ComposeUiTest.open(): Workbench {
        val workbench = Workbench(data = data, state = ReviewState(repos = mapOf(data.repo to Path.of("."))))
        setStretchContent(StretchThemes.WorkshopDark) { ArcFrame(workbench) }
        waitForIdle()
        assertTrue(workbench.addSource(data.source.ref))
        waitUntil { workbench.state.snapshot(data.source) != null }
        waitForIdle()
        return workbench
    }

    private fun ComposeUiTest.shown(text: String) = onAllNodesWithText(text, substring = true).fetchSemanticsNodes().isNotEmpty()

    @Test
    fun addingASourceShowsItsFilesInTheTreeAndTheStrip() = runComposeUiTest {
        open()
        assertTrue(shown("Main.kt"))
        assertTrue(shown("app/util") || shown("util"))
        assertTrue(shown("0/3 reviewed"))
        assertTrue(shown("demo@aaaaaaa..bbbbbbb"))
    }

    @Test
    fun clickingAFileOpensItsDiffAndTheViewSwitchesToTheFullFile() = runComposeUiTest {
        val workbench = open()
        onNodeWithText("Main.kt").performClick()
        waitUntil { workbench.panes.focused != null }
        assertEquals("app/Main.kt", workbench.panes.focused!!.path)
        waitUntil { shown("line twenty") }
        onNodeWithText("Full file").performClick()
        waitForIdle()
        assertEquals(PaneView.FullFile, workbench.panes.focused!!.view)
    }

    @Test
    fun markAndAdvanceByKeyMarksTheFileAndOpensTheNextOne() = runComposeUiTest {
        val workbench = open()
        workbench.panes.openChange(data.source, "app/Main.kt")
        waitUntil { workbench.panes.focused != null }
        workbench.registry[dev.chieppa.arc.ui.actions.ArcActionIds.MARK_AND_ADVANCE]!!.run()
        waitForIdle()
        assertEquals(FileReviewState.Reviewed, workbench.state.states(data.source)["app/Main.kt"])
        assertEquals("docs/Guide.md", workbench.panes.focused!!.path)
        assertTrue(shown("1/3 reviewed"))
    }

    @Test
    fun theRKeyInTheTreeMarksTheSelectionReviewed() = runComposeUiTest {
        val workbench = open()
        onNodeWithText("Main.kt").performClick()
        waitForIdle()
        onAllNodesWithText("Main.kt").onFirst().performKeyInput { pressKey(Key.R) }
        waitForIdle()
        assertEquals(FileReviewState.Reviewed, workbench.state.states(data.source)["app/Main.kt"])
    }

    @Test
    fun theMarkBoxMarksAFileAndAFolderBoxMarksEverythingUnderIt() = runComposeUiTest {
        val workbench = open()
        onAllNodesWithContentDescription("Reviewed").onFirst().performClick()
        waitForIdle()
        val states = workbench.state.states(data.source)
        assertTrue(states.values.any { it == FileReviewState.Reviewed })
    }

    @Test
    fun altDownWalksTheFilesInTreeOrder() = runComposeUiTest {
        val workbench = open()
        onRoot().performKeyInput { withKeyDown(Key.AltLeft) { pressKey(Key.DirectionDown) } }
        waitForIdle()
        val first = workbench.panes.focused?.path
        onRoot().performKeyInput { withKeyDown(Key.AltLeft) { pressKey(Key.DirectionDown) } }
        waitForIdle()
        assertEquals("app/util/Text.kt", first)
        assertEquals("app/Main.kt", workbench.panes.focused?.path)
    }

    @Test
    fun findInFileCountsMatchesWithWholeWordAndRegex() = runComposeUiTest {
        val workbench = open()
        onNodeWithText("Main.kt").performClick()
        waitUntil { shown("line twenty") }
        workbench.registry[dev.chieppa.arc.ui.actions.ArcActionIds.FIND_IN_FILE]!!.run()
        waitUntil { onAllNodesWithContentDescription("Find").fetchSemanticsNodes().isNotEmpty() }
        onAllNodesWithContentDescription("Find").onFirst().performTextInput("find")
        val find = { workbench.paneCommands.getValue(workbench.panes.focused!!.id.value).find }
        waitUntil { find().matches.size == 2 }
        onAllNodesWithContentDescription("Regular expression").onFirst().performClick()
        onAllNodesWithContentDescription("Find").onFirst().performTextInput(" me$")
        waitUntil { find().matches.size == 1 }
        assertTrue(shown("1 of 1 matches"))
    }

    @Test
    fun theFileFinderOpensAFileWithNoChange() = runComposeUiTest {
        val workbench = open()
        onRoot().performKeyInput { withKeyDown(shortcutKey) { pressKey(Key.P) } }
        waitUntil { shown("README.md") }
        onNodeWithText("README.md").performClick()
        waitForIdle()
        assertEquals("README.md", workbench.panes.focused?.path)
        assertEquals(PaneView.Rendered, workbench.panes.focused?.view)
    }

    @Test
    fun aMarkdownFileCanBePinnedAndShowsRendered() = runComposeUiTest {
        val workbench = open()
        val id = workbench.panes.openFile(data.repo, dev.chieppa.arc.core.pane.Revision(data.head.value), "docs/Guide.md", pinned = true)
        waitForIdle()
        assertEquals(PaneView.Rendered, workbench.state.pane(id.value)!!.view)
        assertTrue(shown("Read"))
    }
}

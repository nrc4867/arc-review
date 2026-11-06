package dev.chieppa.arc.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.v2.runComposeUiTest
import dev.chieppa.arc.core.conversation.LineSide
import dev.chieppa.arc.core.mark.FileReviewState
import dev.chieppa.arc.core.pane.PaneView
import dev.chieppa.arc.ui.actions.ArcActionIds
import dev.chieppa.arc.ui.actions.ArcKeys
import dev.chieppa.arc.ui.find.FindMatch
import dev.chieppa.arc.ui.find.ViewerMatches
import dev.chieppa.arc.ui.panes.DiffTextLine
import dev.chieppa.arc.ui.settings.UiPreferences
import dev.chieppa.stretch.code.CodeRange
import dev.chieppa.stretch.diff.DiffMatch
import dev.chieppa.stretch.diff.DiffSide
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class PaneKindsUiTest {
    private val data = FakeReviewData(more = true)

    private fun androidx.compose.ui.test.ComposeUiTest.openFile(path: String) = openReview(data).also {
        assertTrue(it.panes.openChange(data.source, path))
        waitForIdle()
    }

    private fun androidx.compose.ui.test.ComposeUiTest.hasDiffChoice() = onAllNodesWithText("Diff").fetchSemanticsNodes().isNotEmpty()

    @Test
    fun aDeletedFileShowsItsWholeFormerTextUnderADeletedChip() = runComposeUiTest {
        openFile("old/Gone.kt")
        waitUntil { shows("goneToo") }
        assertTrue(shows("Deleted"))
        assertTrue(shows("fun gone() = 1"))
        assertFalse(hasDiffChoice())
    }

    @Test
    fun anAddedFileShowsWholeUnderAnAddedChip() = runComposeUiTest {
        val workbench = openFile("docs/Guide.md")
        waitUntil { shows("Added") }
        assertFalse(hasDiffChoice())
        assertTrue(shows("Full file") && shows("Rendered"))
        assertEquals("docs/Guide.md", workbench.panes.focused?.path)
    }

    @Test
    fun aFileWithNoChangedLineShowsWholeUnderAnUnchangedChip() = runComposeUiTest {
        openFile("scripts/run.sh")
        waitUntil { shows("echo run") }
        assertTrue(shows("Unchanged"))
        assertFalse(hasDiffChoice())
    }

    @Test
    fun aBinaryFileShowsItsKindInTheHeaderAndTheBody() = runComposeUiTest {
        openFile("img/logo.png")
        waitUntil { shows("Binary file") }
        assertTrue(shows("Added"))
        assertTrue(onAllNodesWithText("Binary").fetchSemanticsNodes().isNotEmpty())
    }

    @Test
    fun aChangedFileStillOffersItsDiff() = runComposeUiTest {
        openFile("app/Main.kt")
        waitUntil { shows("line twenty") }
        assertTrue(hasDiffChoice())
        assertFalse(shows("Unchanged"))
    }

    @Test
    fun markReviewedIsAToggleThatMarksAndAdvancesAndUnmarks() = runComposeUiTest {
        val workbench = openFile("app/Main.kt")
        onNodeWithText("Mark reviewed").performClick()
        waitForIdle()
        assertEquals(FileReviewState.Reviewed, workbench.state.states(data.source)["app/Main.kt"])
        assertEquals("docs/Guide.md", workbench.panes.focused?.path)
        workbench.panes.openChange(data.source, "app/Main.kt")
        waitForIdle()
        onNodeWithText("Mark reviewed").performClick()
        waitForIdle()
        assertEquals(FileReviewState.NotReviewed, workbench.state.states(data.source)["app/Main.kt"])
    }

    @Test
    fun theWrapButtonAndItsCommandWrapThePaneAndTheSettingSetsTheDefault() = runComposeUiTest {
        val workbench = openFile("app/Main.kt")
        val id = workbench.panes.focused!!.id.value
        assertFalse(workbench.wrap(id))
        onAllNodesWithContentDescription("Wrap long lines").onFirst().performClick()
        waitForIdle()
        assertTrue(workbench.wrap(id))
        workbench.registry[ArcActionIds.TOGGLE_WRAP]!!.run()
        assertFalse(workbench.wrap(id))
        assertEquals(ArcKeys.TOGGLE_WRAP, workbench.keymap.keysFor(ArcActionIds.TOGGLE_WRAP).first())
        workbench.preferences = UiPreferences(wrapLines = true)
        assertFalse(workbench.wrap(id))
        assertTrue(workbench.wrap("p99"))
    }

    @Test
    fun findInAFullFileCountsMatchesWithoutWashingLines() = runComposeUiTest {
        val workbench = openFile("app/Main.kt")
        workbench.panes.setView(workbench.panes.focused!!.id.value, PaneView.FullFile)
        waitUntil { shows("find me too") }
        workbench.registry[ArcActionIds.FIND_IN_FILE]!!.run()
        waitUntil { onAllNodesWithContentDescription("Find").fetchSemanticsNodes().isNotEmpty() }
        onAllNodesWithContentDescription("Find").onFirst().performTextInput("find me")
        waitUntil { shows("1 of 2") }
    }

    @Test
    fun findMatchesLandOnTheirSideOfTheDiffAndTheirLineOfTheFile() {
        val lines = listOf(DiffTextLine(LineSide.Old, 20, "line 20"), DiffTextLine(LineSide.New, 20, "line twenty"))
        val found = listOf(FindMatch(0, 0, 4), FindMatch(1, 5, 11))
        assertEquals(listOf(DiffMatch(0, DiffSide.Old, 20, 0, 4), DiffMatch(0, DiffSide.New, 20, 5, 11)), ViewerMatches.inDiff(found, lines))
        assertEquals(listOf(CodeRange(1, 0, 4), CodeRange(2, 5, 11)), ViewerMatches.inCode(found))
    }
}

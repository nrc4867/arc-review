package dev.chieppa.arc.ui

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performMouseInput
import androidx.compose.ui.test.rightClick
import androidx.compose.ui.test.v2.runComposeUiTest
import dev.chieppa.arc.core.pane.Page
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.ui.actions.ArcActionIds
import dev.chieppa.arc.ui.drawers.DrawerIds
import dev.chieppa.arc.ui.settings.StagedPlace
import dev.chieppa.arc.ui.start.StartCards
import dev.chieppa.arc.ui.util.SourceLabels
import dev.chieppa.arc.ui.workbench.Workbench
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class SourceHomeUiTest {
    private val data = FakeReviewData()

    private fun ComposeUiTest.rootRow(source: Source = data.source) = onNodeWithText(SourceLabels.of(source) + "  Rotate tokens")

    private fun ComposeUiTest.shown(card: String): Boolean = onAllNodesWithTag(card).fetchSemanticsNodes().isNotEmpty()

    private fun Workbench.home(): Page.Home? = state.workspace.pages.filterIsInstance<Page.Home>().singleOrNull()

    @Test
    fun clickingASourcesRootRowOpensItsHomeEveryTime() = runComposeUiTest {
        val workbench = openReview(data)
        rootRow().performClick()
        waitForIdle()
        val home = workbench.home()!!
        assertEquals(data.source, home.source)
        assertEquals(home.id.value, workbench.paneLayout.activeTab)
        assertTrue(shown(StartCards.THIS_SOURCE))

        workbench.panes.close(home.id.value)
        waitForIdle()
        assertEquals(null, workbench.home())
        rootRow().performClick()
        waitForIdle()
        assertEquals(workbench.home()?.id?.value, workbench.paneLayout.activeTab)
    }

    @Test
    fun theRootRowsMenuOpensTheHomeAndTheHomeIsATab() = runComposeUiTest {
        val workbench = openReview(data)
        rootRow().performMouseInput { rightClick() }
        waitForIdle()
        onNodeWithText("Open home").performClick()
        waitForIdle()
        assertTrue(workbench.home() != null)
        assertTrue(shows("Home · "))
    }

    @Test
    fun openingTheHomeOfAnotherSourceMakesItActive() = runComposeUiTest {
        val workbench = openReview(data)
        val pull = Source.PullRequest(data.repo, 7)
        assertTrue(workbench.addSourceBeside(pull.ref))
        assertWhenIdle { workbench.state.snapshot(pull) != null }
        assertEquals(data.source, workbench.state.active)
        workbench.withTarget(dev.chieppa.arc.ui.workbench.MenuTarget.OfSource(pull)) { workbench.registry[ArcActionIds.OPEN_SOURCE_HOME]!!.run() }
        waitForIdle()
        assertEquals(pull, workbench.state.active)
        assertEquals(pull, workbench.home()?.source)
    }

    @Test
    fun aPullRequestsHomeHoldsTheStagedCommentsAndTheRailDoesNot() = runComposeUiTest {
        val workbench = openReview(data)
        val pull = Source.PullRequest(data.repo, 7)
        assertTrue(workbench.addSource(pull.ref))
        assertWhenIdle { workbench.state.snapshot(pull) != null }
        workbench.openHome(pull)
        waitForIdle()
        assertTrue(shown(StartCards.STAGED))
        assertTrue(shows("Verdict") && shows("Summary"))
        assertFalse(workbench.registry[ArcActionIds.toggleDrawer(DrawerIds.STAGED_COMMENTS)]!!.isAvailable())

        workbench.preferences = workbench.preferences.copy(stagedPlace = StagedPlace.Rail)
        waitForIdle()
        assertFalse(shown(StartCards.STAGED))
        assertTrue(workbench.registry[ArcActionIds.toggleDrawer(DrawerIds.STAGED_COMMENTS)]!!.isAvailable())
    }

    @Test
    fun aSourceWithNothingStagedAndNoVerdictHasNoStagedCard() = runComposeUiTest {
        val workbench = openReview(data)
        workbench.openHome(data.source)
        waitForIdle()
        assertTrue(shown(StartCards.THIS_SOURCE))
        assertFalse(shown(StartCards.STAGED))
    }
}

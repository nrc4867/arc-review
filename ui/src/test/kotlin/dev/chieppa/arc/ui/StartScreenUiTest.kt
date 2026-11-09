package dev.chieppa.arc.ui

import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.core.source.BranchName
import dev.chieppa.arc.git.repo.LocalBranch
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performMouseInput
import androidx.compose.ui.test.rightClick
import androidx.compose.ui.test.v2.runComposeUiTest
import dev.chieppa.arc.core.start.StartCard
import dev.chieppa.arc.core.start.StartColumn
import dev.chieppa.arc.core.start.StartContribution
import dev.chieppa.arc.core.start.StartRow
import dev.chieppa.arc.core.start.StartRowAddition
import dev.chieppa.arc.core.start.StartScreenKind
import dev.chieppa.arc.core.watch.SavedFilter
import dev.chieppa.arc.core.watch.Watchlist
import dev.chieppa.arc.ui.frame.ArcFrame
import dev.chieppa.arc.ui.pulls.FilterResult
import dev.chieppa.arc.ui.pulls.PullWatch
import dev.chieppa.arc.ui.review.WorkspaceState
import dev.chieppa.arc.ui.start.StartCards
import dev.chieppa.arc.ui.start.StartReview
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.test.setStretchContent
import dev.chieppa.stretch.theme.StretchThemes
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class StartScreenUiTest {
    private val data = FakeReviewData()

    private fun ComposeUiTest.shown(card: String): Boolean = onAllNodesWithTag(card).fetchSemanticsNodes().isNotEmpty()

    private fun ComposeUiTest.openEmpty(watch: PullWatch = PullWatch()): Workbench {
        val workbench = Workbench(
            dispatchers = testDispatchers(),
            data = data,
            state = WorkspaceState(repos = mapOf(data.repo to Path.of("."))),
            watch = watch,
        )
        setStretchContent(StretchThemes.WorkshopDark) { ArcFrame(workbench) }
        waitForIdle()
        return workbench
    }

    @Test
    fun withNothingOpenItOffersTheOpenFieldTheSavedFiltersAndArcsCards() = runComposeUiTest {
        val watch = PullWatch(Watchlist(listOf(SavedFilter("f1", "Team: auth", "label:auth")))).apply {
            results = mapOf("f1" to FilterResult(7, listOf(FakePulls.pull(412, "Rotate refresh tokens")), 0))
        }
        val workbench = openEmpty(watch)
        assertEquals(StartScreenKind.NothingOpen, workbench.start.kind)
        assertTrue(shows("Find a source"))
        onNodeWithContentDescription("Find a source to open").assertExists()
        assertTrue(shows("1 new"))
        assertTrue(shown(StartCards.REPOSITORIES) && shown(StartCards.KEYS) && shown(StartCards.SETTINGS) && shown(StartCards.HELP))
        assertFalse(shown(StartCards.RECENT))

        onNodeWithText("Team: auth").performClick()
        waitForIdle()
        assertTrue(workbench.state.workspace.pages.isNotEmpty())
        assertFalse(shown(StartCards.REPOSITORIES))
    }

    @Test
    fun typingInTheFieldSuggestsUnderItAndEnterOpensTheHighlightedOne() = runComposeUiTest {
        data.branchList = listOf(LocalBranch("spike/token-binding", "mfischer", 0), LocalBranch("docs/readme", "jpark", 0))
        val workbench = openEmpty()
        val field = onNodeWithContentDescription("Find a source to open")
        field.performTextInput("bindng")
        waitForIdle()
        assertTrue(shows("spike/token-binding"))
        assertFalse(shows("docs/readme"))
        assertFalse(workbench.switcher.open)
        field.performKeyInput { pressKey(Key.Enter) }
        waitForIdle()
        assertEquals(Source.BranchAgainstBase(data.repo, BranchName("spike/token-binding")), workbench.state.active)
    }

    @Test
    fun aRemovedSourceIsRecentAndOpensAgainFromThere() = runComposeUiTest {
        val workbench = openReview(data)
        workbench.removeSource(data.source)
        waitForIdle()
        assertTrue(shown(StartCards.RECENT))
        assertEquals(data.source.ref, workbench.start.recent.sources.single().ref)
        onNodeWithText("Rotate tokens").performClick()
        waitForIdle()
        assertEquals(listOf(data.source), workbench.state.workspace.sources)
    }

    @Test
    fun withOneSourceContinueOpensTheNextFileToReview() = runComposeUiTest {
        val workbench = openReview(data)
        assertEquals(StartScreenKind.OneSource, workbench.start.kind)
        assertTrue(shown(StartCards.THIS_SOURCE) && shown(StartCards.REVISIONS))
        val next = StartReview.nextFile(workbench.state, data.source)!!
        onNodeWithText("Continue with " + next.substringAfterLast('/')).performClick()
        waitForIdle()
        assertEquals(next, workbench.panes.focused?.path)
        assertFalse(shown(StartCards.THIS_SOURCE))
    }

    @Test
    fun aCardsMenuHidesAndMovesItForThisScreenUntilRestored() = runComposeUiTest {
        val workbench = openReview(data)
        assertEquals(listOf(StartCards.REVISIONS, StartCards.KEYS, StartCards.THIS_SOURCE), workbench.start.showing.side.map { it.id })
        onNodeWithTag(StartCards.THIS_SOURCE).performMouseInput { rightClick() }
        waitForIdle()
        onNodeWithText("Move up").performClick()
        waitForIdle()
        assertEquals(listOf(StartCards.REVISIONS, StartCards.THIS_SOURCE, StartCards.KEYS), workbench.start.showing.side.map { it.id })

        onNodeWithTag(StartCards.REVISIONS).performMouseInput { rightClick() }
        waitForIdle()
        onNodeWithText("Hide").performClick()
        waitForIdle()
        assertFalse(shown(StartCards.REVISIONS))
        assertEquals(setOf(StartCards.REVISIONS), workbench.start.layout.of(StartScreenKind.OneSource).hidden)
        assertTrue(workbench.start.layout.of(StartScreenKind.NothingOpen).isDefault)

        onNodeWithTag(StartCards.THIS_SOURCE).performMouseInput { rightClick() }
        waitForIdle()
        onNodeWithText("Move to the other column").performClick()
        waitForIdle()
        assertEquals(StartColumn.Main, workbench.start.showing.columnOf(StartCards.THIS_SOURCE))

        onNodeWithTag(StartCards.THIS_SOURCE).performMouseInput { rightClick() }
        waitForIdle()
        onNodeWithText("Restore start screen cards").performClick()
        waitForIdle()
        assertTrue(shown(StartCards.REVISIONS))
        assertEquals(StartColumn.Side, workbench.start.showing.columnOf(StartCards.THIS_SOURCE))
    }

    @Test
    fun aContributorsCardsRowsAndHidesGoThroughTheSameRegistry() = runComposeUiTest {
        val workbench = openReview(data)
        workbench.start.registry.register("speckit") { context ->
            if (context.kind != StartScreenKind.OneSource || context.sources.single().title != "Rotate tokens") {
                StartContribution()
            } else {
                StartContribution(
                    cards = listOf(StartCard("speckit.specs", "Specs", setOf(StartScreenKind.OneSource), StartColumn.Side, after = StartCards.REVISIONS, rows = listOf(StartRow("spec.md")))),
                    rows = listOf(StartRowAddition(StartCards.THIS_SOURCE, StartRow("Open the spec"))),
                    hide = listOf(StartCards.KEYS),
                )
            }
        }
        waitForIdle()
        assertEquals(listOf(StartCards.REVISIONS, "speckit.specs", StartCards.THIS_SOURCE), workbench.start.showing.side.map { it.id })
        assertTrue(shows("spec.md") && shows("Open the spec"))
        assertEquals(mapOf(StartCards.KEYS to "speckit"), workbench.start.showing.hiddenBy)

        workbench.start.registry.unregister("speckit")
        waitForIdle()
        assertFalse(shows("spec.md"))
        assertTrue(shown(StartCards.KEYS))
    }

    @Test
    fun withSeveralSourcesItShowsTheirProgress() = runComposeUiTest {
        val workbench = openReview(data)
        val other = "demo@" + "c".repeat(40)
        assertTrue(workbench.addSourceBeside(other))
        waitForIdle()
        workbench.state.toggleSelected(workbench.state.workspace.sources.last())
        waitForIdle()
        assertEquals(StartScreenKind.SeveralSources, workbench.start.kind)
        assertTrue(shows("2 sources selected"))
        assertTrue(shown(StartCards.PROGRESS))

        onNodeWithText("Open side by side").performClick()
        waitForIdle()
        assertEquals(2, workbench.paneLayout.leaves.size)
        assertTrue(workbench.state.workspace.panes.all { it.pinned })
    }
}

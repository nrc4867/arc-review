package dev.chieppa.arc.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import dev.chieppa.arc.core.pane.Page
import dev.chieppa.arc.core.review.Review
import dev.chieppa.arc.core.review.ReviewId
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.core.watch.NotifyRule
import dev.chieppa.arc.core.watch.SavedFilter
import dev.chieppa.arc.core.watch.Watchlist
import dev.chieppa.arc.host.github.pulls.CheckState
import dev.chieppa.arc.ui.FakePulls.pull
import dev.chieppa.arc.ui.drawers.DrawerIds
import dev.chieppa.arc.ui.frame.ArcFrame
import dev.chieppa.arc.ui.pulls.FilterResult
import dev.chieppa.arc.ui.pulls.PullWatch
import dev.chieppa.arc.ui.review.ReviewState
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.test.setStretchContent
import dev.chieppa.stretch.theme.StretchThemes
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class SavedFiltersUiTest {
    private val data = FakeReviewData()
    private val filter = SavedFilter("f1", "Team: auth", "org:lumen label:auth")
    private val watch = PullWatch(Watchlist(listOf(filter), listOf(NotifyRule.CommentsOnMyPullRequests("r1"), NotifyRule.NewInFilter("r2", "f1", on = false)))).apply {
        results = mapOf(
            "f1" to FilterResult(
                2,
                listOf(pull(412, "Rotate refresh tokens on every use", checks = CheckState.Failed), pull(415, "Rate-limit /auth/token by family", listOf(FakePulls.security))),
                System.currentTimeMillis(),
            ),
        )
    }

    private fun androidx.compose.ui.test.ComposeUiTest.open(): Workbench {
        val workbench = Workbench(data = data, state = ReviewState(Review(ReviewId("r1"), REVIEW), repos = mapOf(data.repo to Path.of("."))), watch = watch)
        setStretchContent(StretchThemes.WorkshopDark) { ArcFrame(workbench) }
        waitForIdle()
        workbench.gitHubRepos = mapOf(data.repo to FakePulls.api)
        waitForIdle()
        return workbench
    }

    @Test
    fun aSavedFilterOpensAsATabListingItsPullRequestsGroupedByLabel() = runComposeUiTest {
        val workbench = open()
        workbench.panes.openFilter("f1")
        waitForIdle()
        assertTrue(workbench.state.review.pages.single() is Page.Filter)
        assertTrue(shows("Rotate refresh tokens on every use"))
        assertTrue(shows("Rate-limit /auth/token by family"))
        assertTrue(shows("demo#412"))
        assertTrue(shows("security"))
        assertTrue(shows("2 open PRs"))
        assertTrue(shows("2 rules, 1 on"))
        onNodeWithContentDescription("Checks failed").assertExists()

        workbench.panes.openFilter("f1")
        assertEquals(1, workbench.state.review.pages.size)
    }

    @Test
    fun addPutsThePullRequestInTheOpenReviewAndThenShowsTheReviewThatHoldsIt() = runComposeUiTest {
        val workbench = open()
        workbench.panes.openFilter("f1")
        waitForIdle()
        onAllNodesWithText("Add").onFirst().performClick()
        waitForIdle()
        val added = workbench.state.review.sources.single()
        assertTrue(added is Source.PullRequest)
        assertTrue(workbench.state.review.pages.single().id.value in workbench.paneLayout.tabs, "the filter's tab follows into the new source's layout")
        assertEquals(1, onAllNodesWithText("Add").fetchSemanticsNodes().size)
        assertEquals(2, onAllNodesWithText(REVIEW).fetchSemanticsNodes().size, "the review selector and the added pull request's chip")
    }

    @Test
    fun theBellCountsWhatIsUnseenAndOpensThePullRequestsDrawer() = runComposeUiTest {
        val workbench = open()
        watch.unseen = 3
        waitForIdle()
        assertTrue(shows("3"))
        onNodeWithContentDescription("Notifications").performClick()
        waitForIdle()
        assertEquals(0, watch.unseen)
        assertTrue(workbench.frameLayout.drawers.single { it.id == DrawerIds.PULL_REQUESTS }.open)
        assertTrue(shows("Notify me".uppercase()))
        assertTrue(shows("Comments on my PRs"))
        onNodeWithText("Team: auth").performClick()
        waitForIdle()
        assertTrue(shows("Rotate refresh tokens on every use"))
    }
}

private const val REVIEW = "Auth rollout"

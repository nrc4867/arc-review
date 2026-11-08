package dev.chieppa.arc.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performMouseInput
import androidx.compose.ui.test.rightClick
import androidx.compose.ui.unit.dp
import androidx.compose.ui.test.v2.runComposeUiTest
import dev.chieppa.arc.core.pane.Page
import dev.chieppa.arc.core.workspace.Workspace
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.core.watch.NotifyRule
import dev.chieppa.arc.core.watch.SavedFilter
import dev.chieppa.arc.core.watch.Watchlist
import dev.chieppa.arc.host.github.pulls.CheckState
import dev.chieppa.arc.ui.FakePulls.pull
import dev.chieppa.arc.ui.drawers.DrawerIds
import dev.chieppa.arc.ui.frame.ArcFrame
import dev.chieppa.arc.ui.pulls.FilterResult
import dev.chieppa.arc.ui.pulls.PullColumns
import dev.chieppa.arc.ui.pulls.PullWatch
import dev.chieppa.arc.ui.review.WorkspaceState
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.test.setStretchContent
import dev.chieppa.stretch.theme.StretchThemes
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
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

    private val copied = mutableListOf<String>()
    private val opened = mutableListOf<String>()

    private fun androidx.compose.ui.test.ComposeUiTest.open(): Workbench {
        val workbench = Workbench(
            dispatchers = testDispatchers(),
            data = data,
            state = WorkspaceState(Workspace(), repos = mapOf(data.repo to Path.of("."))),
            watch = watch,
            clipboard = { copied += it },
            links = { opened += it },
        )
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
        assertTrue(workbench.state.workspace.pages.single() is Page.Filter)
        assertTrue(shows("Rotate refresh tokens on every use"))
        assertTrue(shows("Rate-limit /auth/token by family"))
        assertTrue(shows("demo#412"))
        assertTrue(shows("security"))
        assertTrue(shows("2 open PRs"))
        assertTrue(shows("2 rules, 1 on"))
        onNodeWithContentDescription("Checks failed").assertExists()

        workbench.panes.openFilter("f1")
        assertEquals(1, workbench.state.workspace.pages.size)
    }

    @Test
    fun clickingAPullRequestOpensItAndThenItsRowShowsItIsOpen() = runComposeUiTest {
        val workbench = open()
        workbench.panes.openFilter("f1")
        waitForIdle()
        assertEquals(0, onAllNodesWithText("Add").fetchSemanticsNodes().size)
        assertEquals(0, onAllNodesWithText("Open").fetchSemanticsNodes().size)
        onNodeWithText("Rate-limit /auth/token by family").performClick()
        waitForIdle()
        assertEquals(Source.PullRequest(data.repo, 415), workbench.state.active)
        assertTrue(workbench.state.workspace.pages.single().id.value in workbench.paneLayout.tabs, "the filter's tab follows into the new source's layout")
        assertEquals(1, onAllNodesWithText("Open").fetchSemanticsNodes().size)
        onNodeWithText("Rotate refresh tokens on every use").performClick()
        waitForIdle()
        assertEquals(Source.PullRequest(data.repo, 412), workbench.state.active)
        assertEquals(2, onAllNodesWithText("Open").fetchSemanticsNodes().size)
        onNodeWithText("Rate-limit /auth/token by family").performClick()
        waitForIdle()
        assertEquals(Source.PullRequest(data.repo, 415), workbench.state.active)
        assertEquals(2, workbench.state.workspace.sources.size)
    }

    @Test
    fun aPullRequestsMenuOpensItOnGitHubAndCopiesItsLink() = runComposeUiTest {
        val workbench = open()
        workbench.panes.openFilter("f1")
        waitForIdle()
        onNodeWithText("Rotate refresh tokens on every use").performMouseInput { rightClick() }
        waitForIdle()
        assertFalse(shows("new review"))
        assertTrue(shows("Open without switching"))
        onNodeWithText("Open on GitHub").performClick()
        waitForIdle()
        onNodeWithText("Rotate refresh tokens on every use").performMouseInput { rightClick() }
        waitForIdle()
        onNodeWithText("Copy").performClick()
        waitForIdle()
        onNodeWithText("Link").performClick()
        waitForIdle()
        val url = watch.results.getValue("f1").pulls.first().url
        assertEquals(listOf(url), opened)
        assertEquals(listOf(url), copied)
        assertTrue(workbench.state.workspace.sources.isEmpty())
    }

    @Test
    fun theCopyMenuCopiesThePullRequestsNumberReferenceAndTitle() = runComposeUiTest {
        val workbench = open()
        workbench.panes.openFilter("f1")
        waitForIdle()
        for (item in listOf("Number", "Reference", "Title")) {
            onNodeWithText("Rotate refresh tokens on every use").performMouseInput { rightClick() }
            waitForIdle()
            onNodeWithText("Copy").performClick()
            waitForIdle()
            onNodeWithText(item).performClick()
            waitForIdle()
        }
        assertEquals(listOf("#412", "lumen/api#412", "Rotate refresh tokens on every use"), copied)
    }

    @Test
    fun openingAPullRequestWithNoCloneClonesItsRepoFirst() = runComposeUiTest {
        data.cloneTo = Path.of("clones", "lumen", "web")
        val workbench = open()
        workbench.panes.openFilter("f1")
        waitForIdle()
        workbench.gitHubRepos = emptyMap()
        waitForIdle()
        onNodeWithText("Rate-limit /auth/token by family").performClick()
        waitForIdle()
        assertEquals(listOf(FakePulls.api), data.cloned)
        val active = workbench.state.active as Source.PullRequest
        assertEquals(415, active.number)
        assertEquals(Path.of("clones", "lumen", "web").toAbsolutePath().normalize(), workbench.state.repos[active.repo])
    }

    @Test
    fun aRepoThatCannotBeClonedIsMarkedOnItsRows() = runComposeUiTest {
        val workbench = open()
        workbench.panes.openFilter("f1")
        waitForIdle()
        workbench.gitHubRepos = emptyMap()
        waitForIdle()
        onNodeWithText("Rate-limit /auth/token by family").performClick()
        waitForIdle()
        assertTrue(workbench.state.workspace.sources.isEmpty())
        assertEquals(setOf(FakePulls.api), workbench.cloneFailed)
    }

    @Test
    fun theTitleKeepsItsRoomAndColumnsGiveWayAsTheListNarrows() {
        assertEquals(PullColumns.ALL, PullColumns.fit(1400.dp))
        val narrow = PullColumns.fit(760.dp)
        assertFalse(narrow.labels)
        assertTrue(narrow.waiting)
        assertFalse(narrow.updated)
        assertEquals(PullColumns(labels = false, waiting = false, comments = false, updated = false), PullColumns.fit(400.dp))
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
        onAllNodesWithText("Team: auth").onFirst().performClick()
        waitForIdle()
        assertTrue(shows("Rotate refresh tokens on every use"))
    }
}


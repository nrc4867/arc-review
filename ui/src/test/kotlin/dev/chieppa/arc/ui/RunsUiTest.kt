package dev.chieppa.arc.ui

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runDesktopComposeUiTest
import androidx.compose.ui.test.v2.runComposeUiTest
import dev.chieppa.arc.core.actions.MineRule
import dev.chieppa.arc.core.actions.RunConclusion
import dev.chieppa.arc.core.actions.RunRef
import dev.chieppa.arc.core.actions.RunStatus
import dev.chieppa.arc.core.actions.RunWatchlist
import dev.chieppa.arc.core.pane.Page
import dev.chieppa.arc.ui.frame.ArcFrame
import dev.chieppa.arc.ui.runs.RunConfirm
import dev.chieppa.arc.ui.runs.RunDetail
import dev.chieppa.arc.ui.runs.RunWatch
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.test.setStretchContent
import dev.chieppa.stretch.theme.StretchThemes
import kotlinx.coroutines.launch
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class RunsUiTest {
    private companion object {
        /** Wide enough for a run list's every column. */
        const val WIDE = 1440
        const val TALL = 900
    }

    private val actions = FakeActions()
    private val mine = FakeActions.run(1, "Release", actor = "nc")
    private val failed = FakeActions.run(2, "Nightly", conclusion = RunConclusion.Failure, actor = "nc")
    private val gated = FakeActions.run(3, "Deploy", status = RunStatus.Waiting, conclusion = null)
    private val other = FakeActions.run(4, "Lint")
    private val watch = RunWatch(RunWatchlist.seeds("Mine", "Waiting on me")).apply {
        login = "nc"
        lists = mapOf("lumen/api" to listOf(mine, failed, gated, other))
        waitingOnMe = setOf(gated.ref)
    }

    private fun ComposeUiTest.open(): Workbench {
        val workbench = Workbench(dispatchers = testDispatchers(), runWatch = watch, actions = actions)
        setStretchContent(StretchThemes.WorkshopDark) { ArcFrame(workbench) }
        waitForIdle()
        return workbench
    }

    private fun ComposeUiTest.shows(text: String): Boolean = onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()

    @Test
    fun mineGroupsTheRunsThatAreThePersonsAndARuleSwitchesOff() = runComposeUiTest {
        val workbench = open()
        workbench.panes.openRuns(RunWatchlist.MINE)
        waitForIdle()
        assertTrue(workbench.state.workspace.pages.single() is Page.Runs)
        assertTrue(shows("Waiting on you"))
        assertTrue(shows("Failed"))
        assertTrue(shows("Done today"))
        assertTrue(shows("Release"))
        assertTrue(shows("Deploy"))
        assertTrue(!shows("Lint"), "a run that is not the person's is not in Mine")
        assertTrue(shows("REST · 4712 of 5000 left this hour") || watch.budget == null)

        onAllNodesWithText("Started by me")[0].performClick()
        waitForIdle()
        assertTrue(MineRule.StartedByMe !in watch.watchlist.mineRules)
        assertTrue(!shows("Release"))
        assertTrue(shows("Deploy"))
    }

    @Test
    fun reRunningAFailedRunAsksFirstAndThenWatchesIt() = runDesktopComposeUiTest(WIDE, TALL) {
        val workbench = open()
        workbench.panes.openRuns(RunWatchlist.MINE)
        waitForIdle()
        onNodeWithText("Re-run failed").performClick()
        waitForIdle()
        assertEquals(RunConfirm.Rerun(failed, failedOnly = true), workbench.runs.confirming)
        assertTrue(actions.writes.isEmpty(), "nothing goes to GitHub before it is confirmed")
        onNodeWithText("Re-run failed jobs").performClick()
        waitForIdle()
        assertEquals(listOf("rerun 2 failedOnly=true debug=false"), actions.writes)
        assertTrue(failed.ref in watch.watchlist.runs)
    }

    @Test
    fun aForksRunShowsWhatItChangesUnderGithubBeforeItIsApproved() = runComposeUiTest {
        val fork = FakeActions.run(5, "CI", conclusion = RunConclusion.ActionRequired, headOwner = "tmoreau")
        watch.lists = mapOf("lumen/api" to listOf(fork))
        watch.waitingOnMe = setOf(fork.ref)
        val workbench = open()
        workbench.runs.ask(RunConfirm.ApproveFork(fork))
        waitForIdle()
        assertTrue(shows("CHANGES UNDER .GITHUB/ · 1"))
        assertTrue(shows("modified .github/workflows/ci.yml"))
        onNodeWithText("Approve and run").performClick()
        waitForIdle()
        assertEquals(listOf("approve fork 5"), actions.writes)
    }

    @Test
    fun runWorkflowBuildsItsFormFromTheWorkflowFileAndOpensTheNewRun() = runComposeUiTest {
        val workbench = open()
        watch.followed = setOf(actions.repo)
        workbench.runs.startDispatch(actions.repo)
        waitForIdle()
        assertTrue(shows("Inputs read from .github/workflows/speckit-implement.yml at 4f2a91c"))
        assertTrue(shows("issue"))
        assertTrue(shows("Plan only, open no PR"))
        val form = workbench.runs.dispatching!!
        assertTrue(!form.ready, "a required input is empty")
        form.values["issue"] = "431"
        waitForIdle()
        assertTrue(shows("dispatch speckit-implement.yml on lumen/api @ main"))
        onAllNodesWithText("Run workflow").fetchSemanticsNodes()
        workbench.scope.launch { workbench.runs.dispatch(form) }
        waitForIdle()
        assertEquals(listOf("dispatch speckit-implement.yml@main {agent=default, dry_run=false, issue=431}"), actions.writes)
        val ref = RunRef("lumen/api", 2302)
        assertTrue(ref in watch.watchlist.runs)
        assertTrue(workbench.state.workspace.pages.any { it is Page.Run && it.run == ref })
    }

    @Test
    fun aRunsPageShowsItsJobsAndTheKeptLogOfAFinishedOne() = runComposeUiTest {
        actions.runs = mutableMapOf("lumen/api" to listOf(failed))
        actions.jobs = mutableMapOf(2L to listOf(FakeActions.job(21, "build"), FakeActions.job(22, "test (ubuntu)", RunConclusion.Failure)))
        watch.lists = mapOf("lumen/api" to listOf(failed))
        val workbench = open()
        workbench.panes.openRun(failed.ref)
        waitForIdle()
        val detail: RunDetail = watch.details.getValue(failed.ref)
        assertEquals(listOf("build", "test (ubuntu)"), detail.jobs.map { it.name })
        assertTrue(shows("test (ubuntu)"))
        assertTrue(shows("Nightly #2"))
        onNodeWithText("Jobs").performClick()
        waitForIdle()
        assertTrue(shows("boom"))
    }
}

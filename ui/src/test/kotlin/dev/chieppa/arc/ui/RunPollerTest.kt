package dev.chieppa.arc.ui

import dev.chieppa.arc.core.actions.PendingDeployment
import dev.chieppa.arc.core.actions.RunConclusion
import dev.chieppa.arc.core.actions.RunNotify
import dev.chieppa.arc.core.actions.RunStatus
import dev.chieppa.arc.core.actions.RunWatchlist
import dev.chieppa.arc.host.github.pulls.Viewer
import dev.chieppa.arc.ui.pulls.PullWatch
import dev.chieppa.arc.ui.pulls.WatchStatus
import dev.chieppa.arc.ui.runs.RunNotifier
import dev.chieppa.arc.ui.runs.RunPoller
import dev.chieppa.arc.ui.runs.RunTimes
import dev.chieppa.arc.ui.runs.RunWatch
import dev.chieppa.arc.ui.services.DesktopNotifier
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RunPollerTest {
    private val actions = FakeActions()
    private var now = FakeActions.NOW.toEpochMilli()
    private val told = mutableListOf<String>()
    private val pulls = PullWatch().apply { viewer = Viewer("nc") }
    private val watch = RunWatch(RunWatchlist.seeds("Mine", "Waiting on me").withNotify(RunNotify.MineSucceeded, true), clock = { now }).apply {
        followed = setOf(actions.repo)
    }
    private val poller = RunPoller(watch, pulls, actions, RunNotifier(pulls, DesktopNotifier { title, _ -> told += title }))

    @Test
    fun aFirstPollSetsOffNothingAndAFailureOfMineIsToldOnce() = runTest {
        val running = FakeActions.run(1, "Nightly", status = RunStatus.InProgress, conclusion = null, actor = "nc")
        actions.runs["lumen/api"] = listOf(running)
        poller.pollLists()
        assertEquals(listOf(running), watch.lists["lumen/api"])
        assertEquals(WatchStatus.Live, watch.status)
        assertEquals("nc", watch.login)
        assertTrue(told.isEmpty())

        actions.runs["lumen/api"] = listOf(running.copy(status = RunStatus.Completed, conclusion = RunConclusion.Failure))
        poller.pollLists()
        assertEquals(1, actions.runsAsked, "the repo is not due again within a minute")
        now += RunTimes.ACTIVE_MILLIS
        poller.pollLists()
        assertEquals(listOf("Nightly failed"), told)
        assertEquals(1, pulls.unseen)
    }

    @Test
    fun aDeploymentThePersonMayApproveWaitsOnThem() = runTest {
        val gated = FakeActions.run(3, "Deploy", status = RunStatus.Waiting, conclusion = null)
        val other = FakeActions.run(4, "Stage", status = RunStatus.Waiting, conclusion = null)
        actions.runs["lumen/api"] = listOf(gated, other)
        actions.pending[3] = listOf(PendingDeployment(1, "production", null, canApprove = true))
        actions.pending[4] = listOf(PendingDeployment(2, "staging", null, canApprove = false))
        poller.pollLists()
        assertEquals(setOf(gated.ref), watch.waitingOnMe)
        assertTrue(told.isEmpty(), "what waits at the first poll sets nothing off")
    }

    @Test
    fun aQuietRepoIsPolledEveryFiveMinutes() = runTest {
        actions.runs["lumen/api"] = listOf(FakeActions.run(1, "Old").copy(updatedAt = FakeActions.NOW.minusSeconds(RunTimes.QUIET_AFTER_SECONDS + 60)))
        poller.pollLists()
        assertEquals(1, watch.quietRepos)
        now += RunTimes.ACTIVE_MILLIS
        poller.pollLists()
        assertEquals(1, actions.runsAsked)
        now += RunTimes.QUIET_MILLIS
        poller.pollLists()
        assertEquals(2, actions.runsAsked)
    }
}

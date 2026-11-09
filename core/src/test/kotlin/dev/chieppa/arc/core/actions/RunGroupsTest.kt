package dev.chieppa.arc.core.actions

import kotlin.test.Test
import kotlin.test.assertEquals

class RunGroupsTest {
    @Test
    fun `runs fall into their groups and old finished runs drop out`() {
        val now = RunSamples.T0.epochSecond + 3600
        val waiting = RunSamples.run(1, status = RunStatus.Waiting, conclusion = null)
        val gated = RunSamples.run(2, status = RunStatus.Waiting, conclusion = null)
        val running = RunSamples.run(3, status = RunStatus.InProgress, conclusion = null)
        val failed = RunSamples.run(4, conclusion = RunConclusion.Failure)
        val done = RunSamples.run(5)
        val old = RunSamples.run(6, updatedAt = RunSamples.T0.minusSeconds(RunGroups.DONE_WINDOW_SECONDS + 10))
        val groups = RunGroups.of(listOf(waiting, gated, running, failed, done, old), setOf(waiting.ref), now)
        assertEquals(listOf(RunGroup.WaitingOnYou, RunGroup.Running, RunGroup.Failed, RunGroup.Done), groups.map { it.group })
        assertEquals(listOf(2L, 3L), groups[1].runs.map { it.id }.sorted())
        assertEquals(listOf(5L), groups[3].runs.map { it.id })
    }
}

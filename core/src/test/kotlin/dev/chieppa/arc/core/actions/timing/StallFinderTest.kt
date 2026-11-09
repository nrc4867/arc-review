package dev.chieppa.arc.core.actions.timing

import dev.chieppa.arc.core.actions.RunSamples
import dev.chieppa.arc.core.actions.RunStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StallFinderTest {
    @Test
    fun `a step is stalled past twice its usual and five minutes over it`() {
        assertTrue(StallRules.step(13 * 60 + 44, 4 * 60 + 30))
        assertFalse(StallRules.step(5 * 60, 2 * 60), "more than twice but three minutes over")
        assertFalse(StallRules.step(30 * 60, null))
    }

    @Test
    fun `a job is stalled in the queue only when it usually starts at once`() {
        assertTrue(StallRules.queue(6 * 60, 30))
        assertFalse(StallRules.queue(6 * 60, 120))
    }

    @Test
    fun `finds the running step that stalled`() {
        val run = RunSamples.run(workflowPath = ".github/workflows/speckit.yml", status = RunStatus.InProgress, conclusion = null)
        val key = TimingKey(run.repo, run.workflowPath, "implement")
        var history = TimingHistory()
        for (id in 1L..5L) history = history.record(key, id, RunSamples.job(id, "implement", listOf(RunSamples.step(1, "Run agent", 0, 270))))
        val job = RunSamples.job(10, "implement", listOf(RunSamples.step(1, "Run agent", 0, null)), conclusion = null)
        val stalls = StallFinder.of(run, listOf(job), history, RunSamples.T0.epochSecond + 824)
        assertEquals(listOf(RunStall(10, 1, 554)), stalls)
    }
}

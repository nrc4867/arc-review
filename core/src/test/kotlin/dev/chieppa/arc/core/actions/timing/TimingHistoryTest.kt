package dev.chieppa.arc.core.actions.timing

import dev.chieppa.arc.core.actions.RunConclusion
import dev.chieppa.arc.core.actions.RunSamples
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TimingHistoryTest {
    private val key = TimingKey("lumen/api", ".github/workflows/ci.yml", "build")

    @Test
    fun `only the last twenty successful runs are kept`() {
        var history = TimingHistory()
        for (run in 1L..25L) history = history.record(key, run, RunSamples.job(run, "build", listOf(RunSamples.step(1, "Compile", 0, run * 10))))
        history = history.record(key, 99, RunSamples.job(99, "build", listOf(RunSamples.step(1, "Compile", 0, 9999)), RunConclusion.Failure))
        assertEquals(20, history.jobs.getValue(key).size)
        assertEquals(230L, history.stepP90(key, "Compile"))
        assertNull(history.stepP90(key, "Deploy"))
    }

    @Test
    fun `percentiles take the nearest rank`() {
        assertEquals(9L, Percentiles.of((1L..10L).toList()))
        assertNull(Percentiles.of(emptyList()))
    }
}

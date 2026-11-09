package dev.chieppa.arc.core.actions

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RunStateTest {
    @Test
    fun `a fork run awaiting approval is waiting`() {
        val run = RunSamples.run(conclusion = RunConclusion.ActionRequired, headOwner = "tmoreau")
        assertEquals(RunState.Waiting, run.state)
        assertTrue(run.awaitsForkApproval)
    }
}

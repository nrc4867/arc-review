package dev.chieppa.arc.core.actions

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class MineRulesTest {
    private val all = MineRule.entries.toSet()

    @Test
    fun `a run the person pushed is theirs`() {
        val run = RunSamples.run(actor = "NC")
        assertEquals(WhyMine.StartedByMe, MineRules.why(run, MineFacts("nc"), all))
    }

    @Test
    fun `a run on a pull request they are assigned to is theirs by its head`() {
        val run = RunSamples.run(headSha = "abc")
        val facts = MineFacts("nc", pulls = listOf(MyPull("lumen/api", 412, "abc", authored = false)))
        assertEquals(WhyMine.AssignedPull, MineRules.why(run, facts, all))
    }

    @Test
    fun `waiting on the person is the strongest reason`() {
        val run = RunSamples.run(actor = "nc")
        val facts = MineFacts("nc", waitingOnMe = setOf(run.ref))
        assertEquals(listOf(WhyMine.WaitingOnMe, WhyMine.StartedByMe), MineRules.reasons(run, facts, all))
    }

    @Test
    fun `a rule switched off finds nothing`() {
        val run = RunSamples.run(workflowPath = ".github/workflows/nightly.yml")
        val facts = MineFacts("nc", workflows = listOf(WatchedWorkflow("lumen/api", ".github/workflows/nightly.yml")))
        assertEquals(WhyMine.Watched, MineRules.why(run, facts, all))
        assertNull(MineRules.why(run, facts, all - MineRule.Watched))
    }
}

package dev.chieppa.arc.core.actions

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RunQueryTest {
    @Test
    fun `every term must match`() {
        val query = RunQuery.parse("repo:lumen/api workflow:ci.yml branch:main")
        assertTrue(query.matches(RunSamples.run(), emptySet()))
        assertFalse(query.matches(RunSamples.run(branch = "dev"), emptySet()))
    }

    @Test
    fun `a star matches any text`() {
        val query = RunQuery.parse("workflow:speckit-*.yml")
        assertTrue(query.matches(RunSamples.run(workflowPath = ".github/workflows/speckit-implement.yml"), emptySet()))
    }

    @Test
    fun `status done covers every finished run and an unknown term matches nothing`() {
        assertTrue(RunQuery.parse("status:done").matches(RunSamples.run(conclusion = RunConclusion.Cancelled), emptySet()))
        assertFalse(RunQuery.parse("colour:red").matches(RunSamples.run(), emptySet()))
    }

    @Test
    fun `waiting on me needs the run to wait on the person`() {
        val run = RunSamples.run(status = RunStatus.Waiting, conclusion = null)
        assertTrue(RunQuery.parse("is:waiting-on-me").matches(run, setOf(run.ref)))
        assertFalse(RunQuery.parse("is:waiting-on-me").matches(run, emptySet()))
    }
}

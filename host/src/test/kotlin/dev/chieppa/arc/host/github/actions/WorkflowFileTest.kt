package dev.chieppa.arc.host.github.actions

import dev.chieppa.arc.core.actions.InputKind
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WorkflowFileTest {
    @Test
    fun `reads a manual trigger's inputs and the jobs' needs`() {
        val spec = WorkflowFile.parse(
            """
            name: speckit · implement
            on:
              workflow_dispatch:
                inputs:
                  issue: { type: number, required: true }
                  agent:
                    type: choice
                    options: [default, large]
                    default: default
                  target: { type: environment }
                  dry_run: { type: boolean, default: false, description: Plan only, open no PR }
                  notes: { description: Passed to the agent as extra context }
            jobs:
              plan: { runs-on: ubuntu-24.04 }
              implement: { needs: plan }
              open-pr: { name: open PR, needs: [plan, implement] }
            """.trimIndent(),
        )!!
        assertTrue(spec.dispatchable)
        val inputs = spec.dispatchInputs!!.associateBy { it.name }
        assertEquals(InputKind.Number, inputs.getValue("issue").kind)
        assertTrue(inputs.getValue("issue").required)
        assertEquals(listOf("default", "large"), inputs.getValue("agent").options)
        assertEquals(InputKind.Environment, inputs.getValue("target").kind)
        assertEquals("false", inputs.getValue("dry_run").default)
        assertEquals(InputKind.Text, inputs.getValue("notes").kind)
        assertEquals(listOf("plan", "implement"), spec.jobs.single { it.id == "open-pr" }.needs)
        assertEquals("open PR", spec.jobs.single { it.id == "open-pr" }.name)
    }

    @Test
    fun `on as a list or a word, and no manual trigger`() {
        assertTrue(WorkflowFile.parse("on: [push, workflow_dispatch]\njobs: {}")!!.dispatchable)
        assertTrue(WorkflowFile.parse("on: workflow_dispatch")!!.dispatchable)
        assertFalse(WorkflowFile.parse("on: push")!!.dispatchable)
        assertNull(WorkflowFile.parse("{ not yaml"))
    }
}

package dev.chieppa.arc.core.actions.graph

import dev.chieppa.arc.core.actions.RunSamples
import dev.chieppa.arc.core.actions.WorkflowJobSpec
import kotlin.test.Test
import kotlin.test.assertEquals

class JobColumnsTest {
    @Test
    fun `jobs line up by what they need`() {
        val specs = listOf(
            WorkflowJobSpec("build", null),
            WorkflowJobSpec("test", "test (\${{ matrix.os }})", listOf("build")),
            WorkflowJobSpec("sql", "sql · migrations", listOf("build")),
            WorkflowJobSpec("deploy", "deploy", listOf("test", "sql")),
        )
        val jobs = listOf("build", "test (ubuntu)", "sql · migrations", "deploy").mapIndexed { i, name -> RunSamples.job(i.toLong(), name, emptyList()) }
        assertEquals(listOf(listOf("build"), listOf("test (ubuntu)", "sql · migrations"), listOf("deploy")), JobColumns.of(jobs, specs).map { col -> col.map { it.name } })
    }

    @Test
    fun `with no specs every job is in one column`() {
        val jobs = listOf(RunSamples.job(1, "a", emptyList()), RunSamples.job(2, "b", emptyList()))
        assertEquals(1, JobColumns.of(jobs, emptyList()).size)
    }
}

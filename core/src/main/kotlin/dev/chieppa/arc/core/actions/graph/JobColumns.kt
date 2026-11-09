package dev.chieppa.arc.core.actions.graph

import dev.chieppa.arc.core.actions.RunJob
import dev.chieppa.arc.core.actions.WorkflowJobSpec

/**
 * Lays a run's jobs out in columns by what they wait for, as the workflow file [specs] declares it: a job with nothing
 * to wait for is in the first column, and every other one column after the last job it needs. A job is matched to its
 * spec by the name it runs under, a matrix job by the name before its parenthesis. Jobs no spec matches, or all jobs
 * when there are no specs, go in the first column.
 */
object JobColumns {
    fun of(jobs: List<RunJob>, specs: List<WorkflowJobSpec>): List<List<RunJob>> {
        if (jobs.isEmpty()) return emptyList()
        val byId = specs.associateBy { it.id }
        val depths = mutableMapOf<String, Int>()
        fun depth(id: String, seen: Set<String>): Int = depths[id] ?: run {
            val spec = byId[id] ?: return 0
            val needs = spec.needs.filter { it in byId && it !in seen }
            (if (needs.isEmpty()) 0 else needs.maxOf { depth(it, seen + id) } + 1).also { depths[id] = it }
        }
        val columns = jobs.groupBy { job -> specFor(job.name, specs)?.let { depth(it.id, emptySet()) } ?: 0 }
        return columns.keys.sorted().map { columns.getValue(it) }
    }

    /** The spec the job named [name] runs under, if one matches. */
    fun specFor(name: String, specs: List<WorkflowJobSpec>): WorkflowJobSpec? {
        val base = name.substringBefore(" (").trim()
        return specs.firstOrNull { shown(it) == name } ?: specs.firstOrNull { shown(it) == base }
            ?: specs.firstOrNull { spec -> shown(spec).substringBefore(EXPRESSION).trim().let { it.isNotEmpty() && name.startsWith(it) } }
    }

    private fun shown(spec: WorkflowJobSpec): String = spec.name ?: spec.id

    private const val EXPRESSION = "\${{"
}

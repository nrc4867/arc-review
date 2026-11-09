package dev.chieppa.arc.core.actions

/**
 * A run filter's query: space-separated terms, each `key:value`, all of which a run must match. `repo:owner/name`,
 * `workflow:` (the workflow's file or name, `*` matching any text), `branch:`, `event:`, `actor:`, `status:`
 * (`waiting`, `queued`, `running`, `success`, `failure`, `cancelled`, `done`) and `is:waiting-on-me`. A term this
 * does not know matches nothing, so a typo shows an empty list rather than every run.
 */
data class RunQuery(val terms: List<Pair<String, String>>) {
    /** The repos the query names, which are polled for it whether followed or not. */
    val repos: List<String> get() = terms.filter { it.first == REPO }.map { it.second }

    fun matches(run: WorkflowRun, waitingOnMe: Set<RunRef>): Boolean = terms.all { (key, value) ->
        when (key) {
            REPO -> run.repo.equals(value, ignoreCase = true)
            WORKFLOW -> glob(value).let { it.matches(run.workflowFile) || it.matches(run.workflowName) || it.matches(run.workflowPath) }
            BRANCH -> glob(value).matches(run.headBranch.orEmpty())
            EVENT -> run.event.equals(value, ignoreCase = true)
            ACTOR -> run.actor.equals(value, ignoreCase = true) || run.triggeringActor.equals(value, ignoreCase = true)
            STATUS -> status(run.state, value)
            IS -> value == WAITING_ON_ME && run.ref in waitingOnMe
            else -> false
        }
    }

    private fun status(state: RunState, value: String): Boolean = when (value.lowercase()) {
        "waiting" -> state == RunState.Waiting
        "queued" -> state == RunState.Queued
        "running", "in_progress" -> state == RunState.Running
        "success", "succeeded" -> state == RunState.Succeeded
        "failure", "failed" -> state == RunState.Failed
        "cancelled" -> state == RunState.Cancelled
        "done", "completed" -> state.finished
        else -> false
    }

    private fun glob(pattern: String): Regex =
        Regex(pattern.split('*').joinToString(".*") { Regex.escape(it) }, RegexOption.IGNORE_CASE)

    companion object {
        const val REPO = "repo"
        const val WORKFLOW = "workflow"
        const val BRANCH = "branch"
        const val EVENT = "event"
        const val ACTOR = "actor"
        const val STATUS = "status"
        const val IS = "is"
        const val WAITING_ON_ME = "waiting-on-me"

        fun parse(text: String): RunQuery = RunQuery(
            text.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }.map { term ->
                val colon = term.indexOf(':')
                if (colon <= 0) "" to term else term.substring(0, colon).lowercase() to term.substring(colon + 1)
            },
        )
    }
}

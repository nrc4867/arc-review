package dev.chieppa.arc.core.actions

/**
 * What a workflow file says that the app uses: its name, its manual trigger's inputs (null when it has no manual
 * trigger) and its jobs.
 */
data class WorkflowSpec(val name: String?, val dispatchInputs: List<WorkflowInput>?, val jobs: List<WorkflowJobSpec> = emptyList()) {
    val dispatchable: Boolean get() = dispatchInputs != null
}

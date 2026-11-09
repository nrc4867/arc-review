package dev.chieppa.arc.core.actions

/** A job as a workflow file declares it: its key, the name it shows under, and the jobs it waits for. */
data class WorkflowJobSpec(val id: String, val name: String?, val needs: List<String> = emptyList())

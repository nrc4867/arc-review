package dev.chieppa.arc.core.actions

/** One input of a workflow's manual trigger, read from its file. [options] are a choice's values. */
data class WorkflowInput(
    val name: String,
    val kind: InputKind,
    val description: String? = null,
    val required: Boolean = false,
    val default: String? = null,
    val options: List<String> = emptyList(),
)

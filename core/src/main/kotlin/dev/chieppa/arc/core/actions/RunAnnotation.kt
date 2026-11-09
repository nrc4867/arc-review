package dev.chieppa.arc.core.actions

/** A message a job left on a line: its [level] (`failure`, `warning` or `notice`), where, and which job. */
data class RunAnnotation(
    val path: String,
    val line: Int,
    val level: String,
    val message: String,
    val job: String,
    val title: String? = null,
) {
    val error: Boolean get() = level == FAILURE

    companion object {
        const val FAILURE = "failure"
        const val WARNING = "warning"
    }
}

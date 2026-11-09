package dev.chieppa.arc.core.actions.artifacts

/** One test of a JUnit report: its class and name, seconds, the failure's message, and where it failed when the trace says. */
data class JUnitCase(
    val className: String,
    val name: String,
    val seconds: Double?,
    val failure: String? = null,
    val location: String? = null,
    val skipped: Boolean = false,
) {
    val failed: Boolean get() = failure != null
}

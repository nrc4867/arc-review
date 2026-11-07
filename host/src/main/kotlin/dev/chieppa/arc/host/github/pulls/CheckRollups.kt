package dev.chieppa.arc.host.github.pulls

import dev.chieppa.arc.host.github.api.JsonFields.nodes
import dev.chieppa.arc.host.github.api.JsonFields.string
import kotlinx.serialization.json.JsonObject

/** Reads a commit's `statusCheckRollup`: its overall state, and the names of the failed checks among its `contexts`. */
object CheckRollups {
    private val FAILED_CONCLUSIONS = setOf("FAILURE", "TIMED_OUT", "CANCELLED", "ACTION_REQUIRED", "STARTUP_FAILURE")
    private val FAILED_STATES = setOf("FAILURE", "ERROR")
    private const val CHECK_RUN = "CheckRun"

    fun parse(rollup: JsonObject?): CheckRollup {
        if (rollup == null) return CheckRollup.NONE
        val failed = rollup.nodes("contexts").mapNotNull { context ->
            if (context.string("__typename") == CHECK_RUN) {
                context.string("name")?.takeIf { context.string("conclusion") in FAILED_CONCLUSIONS }
            } else {
                context.string("context")?.takeIf { context.string("state") in FAILED_STATES }
            }
        }.distinct()
        return CheckRollup(state(rollup.string("state")), failed)
    }

    /** The check state GitHub's rollup state name stands for. */
    fun state(name: String?): CheckState = when (name) {
        "SUCCESS" -> CheckState.Passed
        "FAILURE", "ERROR" -> CheckState.Failed
        "PENDING", "EXPECTED" -> CheckState.Running
        else -> CheckState.None
    }
}

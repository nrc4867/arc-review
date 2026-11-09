package dev.chieppa.arc.host.github.actions

import dev.chieppa.arc.core.actions.RunConclusion
import dev.chieppa.arc.core.actions.RunStatus
import dev.chieppa.arc.host.github.api.JsonFields.string
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.longOrNull

/** Reading the Actions REST answers' own kinds of field. */
object ActionsJson {
    fun JsonObject?.long(name: String): Long? = (this?.get(name) as? JsonPrimitive)?.longOrNull

    fun status(text: String?): RunStatus = when (text) {
        "requested" -> RunStatus.Requested
        "pending" -> RunStatus.Pending
        "waiting" -> RunStatus.Waiting
        "in_progress" -> RunStatus.InProgress
        "completed" -> RunStatus.Completed
        else -> RunStatus.Queued
    }

    fun conclusion(text: String?): RunConclusion? = when (text) {
        "success" -> RunConclusion.Success
        "failure" -> RunConclusion.Failure
        "cancelled" -> RunConclusion.Cancelled
        "skipped" -> RunConclusion.Skipped
        "timed_out" -> RunConclusion.TimedOut
        "action_required" -> RunConclusion.ActionRequired
        "neutral" -> RunConclusion.Neutral
        "stale" -> RunConclusion.Stale
        "startup_failure" -> RunConclusion.StartupFailure
        else -> null
    }

    fun login(obj: JsonObject?): String? = obj.string("login")
}

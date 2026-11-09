package dev.chieppa.arc.host.github.actions

import dev.chieppa.arc.core.actions.RunJob
import dev.chieppa.arc.core.actions.RunStep
import dev.chieppa.arc.host.github.actions.ActionsJson.long
import dev.chieppa.arc.host.github.api.JsonFields.array
import dev.chieppa.arc.host.github.api.JsonFields.instant
import dev.chieppa.arc.host.github.api.JsonFields.int
import dev.chieppa.arc.host.github.api.JsonFields.objects
import dev.chieppa.arc.host.github.api.JsonFields.string
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/** Reads a run's jobs and their steps from GitHub's REST answer. */
object JobParser {
    fun list(body: JsonObject): List<RunJob> = body.objects("jobs").mapNotNull(::job)

    fun job(json: JsonObject): RunJob? {
        val id = json.long("id") ?: return null
        return RunJob(
            id = id,
            runId = json.long("run_id") ?: 0,
            name = json.string("name").orEmpty(),
            status = ActionsJson.status(json.string("status")),
            conclusion = ActionsJson.conclusion(json.string("conclusion")),
            createdAt = json.instant("created_at"),
            startedAt = json.instant("started_at"),
            completedAt = json.instant("completed_at"),
            steps = json.objects("steps").map { step ->
                RunStep(
                    number = step.int("number") ?: 0,
                    name = step.string("name").orEmpty(),
                    status = ActionsJson.status(step.string("status")),
                    conclusion = ActionsJson.conclusion(step.string("conclusion")),
                    startedAt = step.instant("started_at"),
                    completedAt = step.instant("completed_at"),
                )
            },
            labels = json.array("labels").mapNotNull { (it as? JsonPrimitive)?.content },
            runner = json.string("runner_name"),
        )
    }
}

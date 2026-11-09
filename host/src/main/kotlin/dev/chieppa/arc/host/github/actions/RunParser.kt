package dev.chieppa.arc.host.github.actions

import dev.chieppa.arc.core.actions.WorkflowRun
import dev.chieppa.arc.host.github.actions.ActionsJson.long
import dev.chieppa.arc.host.github.api.JsonFields.instant
import dev.chieppa.arc.host.github.api.JsonFields.int
import dev.chieppa.arc.host.github.api.JsonFields.obj
import dev.chieppa.arc.host.github.api.JsonFields.objects
import dev.chieppa.arc.host.github.api.JsonFields.string
import kotlinx.serialization.json.JsonObject

/** Reads workflow runs from GitHub's REST answers. */
object RunParser {
    /** The runs of a `workflow_runs` list. */
    fun list(body: JsonObject): List<WorkflowRun> = body.objects("workflow_runs").mapNotNull(::run)

    /** One run, or null when a field it needs is missing. */
    fun run(json: JsonObject): WorkflowRun? {
        val repo = json.obj("repository").string("full_name") ?: return null
        val id = json.long("id") ?: return null
        val created = json.instant("created_at") ?: return null
        return WorkflowRun(
            repo = repo,
            id = id,
            workflowId = json.long("workflow_id") ?: 0,
            workflowName = json.string("name") ?: json.string("path").orEmpty(),
            workflowPath = json.string("path").orEmpty().substringBefore('@'),
            title = json.string("display_title").orEmpty(),
            number = json.int("run_number") ?: 0,
            attempt = json.int("run_attempt") ?: 1,
            event = json.string("event").orEmpty(),
            status = ActionsJson.status(json.string("status")),
            conclusion = ActionsJson.conclusion(json.string("conclusion")),
            headBranch = json.string("head_branch"),
            headSha = json.string("head_sha").orEmpty(),
            actor = ActionsJson.login(json.obj("actor")),
            triggeringActor = ActionsJson.login(json.obj("triggering_actor")),
            createdAt = created,
            startedAt = json.instant("run_started_at"),
            updatedAt = json.instant("updated_at") ?: created,
            pulls = json.objects("pull_requests").mapNotNull { it.int("number") },
            headOwner = json.obj("head_repository").obj("owner").string("login"),
            htmlUrl = json.string("html_url").orEmpty(),
        )
    }
}

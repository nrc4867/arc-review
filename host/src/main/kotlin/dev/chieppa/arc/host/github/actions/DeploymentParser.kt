package dev.chieppa.arc.host.github.actions

import dev.chieppa.arc.core.actions.PendingDeployment
import dev.chieppa.arc.host.github.actions.ActionsJson.long
import dev.chieppa.arc.host.github.api.JsonFields.boolean
import dev.chieppa.arc.host.github.api.JsonFields.instant
import dev.chieppa.arc.host.github.api.JsonFields.obj
import dev.chieppa.arc.host.github.api.JsonFields.objects
import dev.chieppa.arc.host.github.api.JsonFields.string
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

/** Reads the environments a run waits at from GitHub's REST answer. */
object DeploymentParser {
    fun list(body: JsonElement): List<PendingDeployment> = (body as? JsonArray).orEmpty().mapNotNull { item ->
        val json = item as? JsonObject ?: return@mapNotNull null
        val environment = json.obj("environment")
        PendingDeployment(
            environmentId = environment.long("id") ?: return@mapNotNull null,
            environment = environment.string("name").orEmpty(),
            waitingSince = json.instant("wait_timer_started_at"),
            canApprove = json.boolean("current_user_can_approve") ?: false,
            reviewers = json.objects("reviewers").mapNotNull { reviewer ->
                val who = reviewer.obj("reviewer")
                who.string("login") ?: who.string("slug")
            },
        )
    }
}

package dev.chieppa.arc.host.github.review

import dev.chieppa.arc.host.github.api.GitHubClient
import dev.chieppa.arc.host.github.api.JsonFields.boolean
import dev.chieppa.arc.host.github.api.JsonFields.nodes
import dev.chieppa.arc.host.github.api.JsonFields.obj
import dev.chieppa.arc.host.github.api.JsonFields.string
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

/** Sends [ReviewMutations] through [client], each spaced from the last by [pacer]. */
class ReviewWriter(private val client: GitHubClient, private val pacer: MutationPacer) {
    /** The `data` of [mutation] run with [input]. */
    suspend fun mutate(mutation: GraphQlMutation, input: JsonObject): JsonObject {
        pacer.pace()
        return client.graphql(mutation.text, buildJsonObject { put(GraphQlMutation.INPUT, input) })
    }

    /** The node id of the person's pending review on pull request [pullId], or null when they have none. */
    suspend fun pendingReview(pullId: String): String? {
        val data = client.graphql(ReviewMutations.PENDING_REVIEW, buildJsonObject { put(ReviewMutations.PULL, JsonPrimitive(pullId)) })
        return data.obj("node").nodes("reviews").firstOrNull { it.boolean("viewerDidAuthor") == true }.string("id")
    }
}

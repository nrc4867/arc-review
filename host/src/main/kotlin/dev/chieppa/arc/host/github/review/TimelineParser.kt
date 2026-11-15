package dev.chieppa.arc.host.github.review

import dev.chieppa.arc.core.conversation.HeadForcePush
import dev.chieppa.arc.core.conversation.PullCommit
import dev.chieppa.arc.host.github.api.JsonFields.instant
import dev.chieppa.arc.host.github.api.JsonFields.obj
import dev.chieppa.arc.host.github.api.JsonFields.string
import kotlinx.serialization.json.JsonObject

/** Reads the commits and force pushes of a pull request's timeline, as [ConversationQuery] asks for them. */
object TimelineParser {
    private const val TYPE = "__typename"
    private const val COMMIT = "PullRequestCommit"
    private const val FORCE_PUSH = "HeadRefForcePushedEvent"

    /** The commits in [nodes], oldest first; one without a sha is left out. */
    fun commits(nodes: List<JsonObject>): List<PullCommit> = nodes.filter { it.string(TYPE) == COMMIT }.mapNotNull { node ->
        val commit = node.obj("commit") ?: return@mapNotNull null
        val sha = commit.string("oid") ?: return@mapNotNull null
        val author = commit.obj("author")
        val user = author.obj("user")
        val at = commit.instant("committedDate") ?: commit.instant("authoredDate")
        PullCommit(
            sha = sha,
            headline = commit.string("messageHeadline").orEmpty(),
            author = user.string("login") ?: author.string("name") ?: GitHubEnums.GHOST,
            committedAt = at?.epochSecond ?: 0,
            avatar = user.string("avatarUrl"),
        )
    }.sortedBy { it.committedAt }

    /** The force pushes in [nodes], oldest first. */
    fun forcePushes(nodes: List<JsonObject>): List<HeadForcePush> = nodes.filter { it.string(TYPE) == FORCE_PUSH }.mapNotNull { node ->
        val at = node.instant("createdAt") ?: return@mapNotNull null
        val actor = node.obj("actor")
        HeadForcePush(
            actor = actor.string("login") ?: GitHubEnums.GHOST,
            before = node.obj("beforeCommit").string("oid"),
            after = node.obj("afterCommit").string("oid"),
            at = at.epochSecond,
            avatar = actor.string("avatarUrl"),
        )
    }.sortedBy { it.at }
}

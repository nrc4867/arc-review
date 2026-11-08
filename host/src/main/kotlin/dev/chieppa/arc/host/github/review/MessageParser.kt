package dev.chieppa.arc.host.github.review

import dev.chieppa.arc.core.conversation.ReactionGroup
import dev.chieppa.arc.core.conversation.ThreadMessage
import dev.chieppa.arc.host.github.api.JsonFields.boolean
import dev.chieppa.arc.host.github.api.JsonFields.instant
import dev.chieppa.arc.host.github.api.JsonFields.int
import dev.chieppa.arc.host.github.api.JsonFields.obj
import dev.chieppa.arc.host.github.api.JsonFields.objects
import dev.chieppa.arc.host.github.api.JsonFields.string
import kotlinx.serialization.json.JsonObject
import java.time.Instant

/** Reads a comment, review or pull request body as a [ThreadMessage]; a deleted author reads as GitHub's ghost. */
object MessageParser {
    fun parse(node: JsonObject, at: Instant? = node.instant("createdAt")): ThreadMessage {
        val author = node.obj("author")
        return ThreadMessage(
            author = author.string("login") ?: GitHubEnums.GHOST,
            body = node.string("body").orEmpty(),
            writtenAt = at?.epochSecond ?: 0,
            id = node.string("id").orEmpty(),
            avatar = author.string("avatarUrl"),
            reactions = reactions(node),
        )
    }

    /** The reactions on [node] that anyone left, in [dev.chieppa.arc.core.conversation.Reaction] order. */
    fun reactions(node: JsonObject): List<ReactionGroup> = node.objects("reactionGroups").mapNotNull { group ->
        val reaction = ReactionContent.parse(group.string("content")) ?: return@mapNotNull null
        val count = group.obj("reactors").int("totalCount") ?: 0
        if (count > 0) ReactionGroup(reaction, count, group.boolean("viewerHasReacted") ?: false) else null
    }.sortedBy { it.reaction.ordinal }
}

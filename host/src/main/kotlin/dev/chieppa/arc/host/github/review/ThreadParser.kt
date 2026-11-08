package dev.chieppa.arc.host.github.review

import dev.chieppa.arc.core.change.ObjectId
import dev.chieppa.arc.core.conversation.CommentAnchor
import dev.chieppa.arc.core.conversation.LocalComment
import dev.chieppa.arc.core.conversation.LocalKind
import dev.chieppa.arc.core.conversation.ReviewThread
import dev.chieppa.arc.core.conversation.ThreadState
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.host.github.api.JsonFields.boolean
import dev.chieppa.arc.host.github.api.JsonFields.instant
import dev.chieppa.arc.host.github.api.JsonFields.int
import dev.chieppa.arc.host.github.api.JsonFields.nodes
import dev.chieppa.arc.host.github.api.JsonFields.obj
import dev.chieppa.arc.host.github.api.JsonFields.string
import kotlinx.serialization.json.JsonObject

/**
 * Reads a pull request's review threads. Comments in the person's pending review are kept apart as staged comments read
 * back from it; a thread holding nothing else is left out.
 */
object ThreadParser {
    /** The line a thread on a whole file is placed on. */
    const val FILE_LINE = 1

    /** The threads among [threads] and the person's pending comments in them, each on [source]. */
    fun parse(threads: List<JsonObject>, source: Source): Pair<List<ReviewThread>, List<LocalComment>> {
        val parsed = mutableListOf<ReviewThread>()
        val pending = mutableListOf<LocalComment>()
        for (node in threads) {
            val (mine, posted) = node.nodes("comments").partition(::isPending)
            mine.mapNotNullTo(pending) { pendingComment(node, it, source) }
            thread(node, posted, source)?.let(parsed::add)
        }
        return parsed to pending
    }

    private fun isPending(comment: JsonObject): Boolean =
        comment.string("state") == GitHubEnums.PENDING && comment.boolean("viewerDidAuthor") == true

    private fun thread(node: JsonObject, comments: List<JsonObject>, source: Source): ReviewThread? {
        val id = node.string("id") ?: return null
        val path = node.string("path") ?: return null
        if (comments.isEmpty()) return null
        val outdated = node.boolean("isOutdated") ?: false
        val resolved = node.boolean("isResolved") ?: false
        return ReviewThread(
            id = id,
            source = source,
            path = path,
            line = threadLine(node, outdated),
            side = GitHubEnums.side(node.string("diffSide")),
            state = when {
                resolved -> ThreadState.Resolved
                outdated -> ThreadState.Addressed
                else -> ThreadState.Open
            },
            messages = comments.map { MessageParser.parse(it) },
            madeOn = oid(comments.first().obj("originalCommit")),
            startLine = when {
                isFile(node) -> null
                outdated -> node.int("originalStartLine")
                else -> node.int("startLine")
            },
        )
    }

    /** `line` while the thread is current, else `originalLine`; a thread on a whole file is on its first line. */
    private fun threadLine(node: JsonObject, outdated: Boolean): Int = when {
        isFile(node) -> FILE_LINE
        outdated -> node.int("originalLine") ?: node.int("line")
        else -> node.int("line") ?: node.int("originalLine")
    } ?: FILE_LINE

    private fun isFile(node: JsonObject): Boolean = node.string("subjectType") == GitHubEnums.FILE

    private fun pendingComment(thread: JsonObject, comment: JsonObject, source: Source): LocalComment? {
        val id = comment.string("id") ?: return null
        val path = thread.string("path") ?: return null
        val anchor = CommentAnchor(
            path = path,
            line = comment.int("line") ?: comment.int("originalLine") ?: threadLine(thread, outdated = false),
            side = GitHubEnums.side(thread.string("diffSide")),
            startLine = comment.int("startLine") ?: comment.int("originalStartLine"),
            madeOn = oid(comment.obj("commit")) ?: oid(comment.obj("originalCommit")),
        )
        return LocalComment(
            id = id,
            source = source,
            kind = LocalKind.Staged,
            body = comment.string("body").orEmpty(),
            writtenAt = comment.instant("createdAt")?.epochSecond ?: 0,
            anchor = anchor,
            inReplyTo = if (comment.obj("replyTo") != null) thread.string("id") else null,
            fromPending = true,
        )
    }

    private fun oid(commit: JsonObject?): ObjectId? = commit.string("oid")?.let { text ->
        try {
            ObjectId(text)
        } catch (_: IllegalArgumentException) {
            null
        }
    }
}

package dev.chieppa.arc.store.conversation

import dev.chieppa.arc.core.change.ObjectId
import dev.chieppa.arc.core.conversation.CommentAnchor
import dev.chieppa.arc.core.conversation.LineSide
import dev.chieppa.arc.core.conversation.LocalComment
import dev.chieppa.arc.core.conversation.LocalConversation
import dev.chieppa.arc.core.conversation.LocalKind
import dev.chieppa.arc.core.conversation.ReviewSummary
import dev.chieppa.arc.core.conversation.Verdict
import dev.chieppa.arc.core.source.SourceRefs
import dev.chieppa.arc.store.files.StoreJson
import java.nio.file.Path

/**
 * The person's own drafts, staged comments, notes and summaries, kept in a JSON file at [path]. Comments read back
 * from a review pending on the host are not written: the host holds them. Anything that does not read is left out.
 */
class ConversationFile(private val path: Path) {
    fun load(): LocalConversation {
        val stored = StoreJson.load(path, StoredConversation.serializer()) ?: return LocalConversation()
        val comments = stored.comments.distinctBy { it.id }.mapNotNull(::toComment)
        val summaries = stored.summaries.mapNotNull { summary ->
            val source = SourceRefs.parse(summary.source) ?: return@mapNotNull null
            val verdict = Verdict.entries.firstOrNull { it.name == summary.verdict } ?: Verdict.Comment
            source to ReviewSummary(verdict, summary.body)
        }.toMap()
        return LocalConversation(comments, summaries)
    }

    fun save(conversation: LocalConversation) {
        val stored = StoredConversation(
            comments = conversation.comments.filterNot { it.fromPending }.map(::toStored),
            summaries = conversation.summaries.map { (source, summary) -> StoredSummary(source.ref, summary.verdict.name, summary.body) },
        )
        StoreJson.save(path, StoredConversation.serializer(), stored)
    }

    private fun toComment(stored: StoredComment): LocalComment? {
        val source = SourceRefs.parse(stored.source) ?: return null
        val kind = LocalKind.entries.firstOrNull { it.name == stored.kind } ?: return null
        val anchor = stored.path?.let { path ->
            val line = stored.line ?: return null
            val side = LineSide.entries.firstOrNull { it.name == stored.side } ?: LineSide.New
            val madeOn = stored.madeOn?.let { runCatching { ObjectId(it) }.getOrNull() }
            CommentAnchor(path, line, side, stored.startLine, madeOn)
        }
        return LocalComment(stored.id, source, kind, stored.body, stored.writtenAt, anchor, stored.inReplyTo, failed = stored.failed, editedAt = stored.editedAt)
    }

    private fun toStored(comment: LocalComment): StoredComment = StoredComment(
        id = comment.id,
        source = comment.source.ref,
        kind = comment.kind.name,
        body = comment.body,
        writtenAt = comment.writtenAt,
        path = comment.anchor?.path,
        line = comment.anchor?.line,
        side = comment.anchor?.side?.name,
        startLine = comment.anchor?.startLine,
        madeOn = comment.anchor?.madeOn?.value,
        inReplyTo = comment.inReplyTo,
        failed = comment.failed,
        editedAt = comment.editedAt,
    )
}

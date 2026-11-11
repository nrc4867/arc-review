package dev.chieppa.arc.core.conversation

import dev.chieppa.arc.core.source.Source

/**
 * Something the person wrote on [source], kept on this machine until they push it. With an [anchor] it sits on lines
 * of a file; without one it is on the source as a whole: a note on it, or a comment on the pull request's
 * conversation. [inReplyTo] names the host's thread a reply goes under. [fromPending] marks a comment read back from a
 * review the person started on the host. [writtenAt], and [editedAt] once its text has changed since, are in epoch
 * seconds. [failed] marks one the last push could not send.
 */
data class LocalComment(
    val id: String,
    val source: Source,
    val kind: LocalKind,
    val body: String,
    val writtenAt: Long,
    val anchor: CommentAnchor? = null,
    val inReplyTo: String? = null,
    val fromPending: Boolean = false,
    val failed: Boolean = false,
    val editedAt: Long? = null,
) {
    val isNote: Boolean get() = kind == LocalKind.Note
    val isReply: Boolean get() = inReplyTo != null

    /** When its text last changed: [editedAt], or [writtenAt] when it has not changed since. */
    val updatedAt: Long get() = editedAt ?: writtenAt
}

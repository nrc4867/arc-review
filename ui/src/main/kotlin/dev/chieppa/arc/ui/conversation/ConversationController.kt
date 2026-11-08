package dev.chieppa.arc.ui.conversation

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.chieppa.arc.core.conversation.CommentAnchor
import dev.chieppa.arc.core.conversation.ConversationEdits
import dev.chieppa.arc.core.conversation.LocalComment
import dev.chieppa.arc.core.conversation.LocalConversation
import dev.chieppa.arc.core.conversation.LocalKind
import dev.chieppa.arc.core.conversation.Reaction
import dev.chieppa.arc.core.conversation.Reactions
import dev.chieppa.arc.core.conversation.ReviewSummary
import dev.chieppa.arc.core.conversation.ReviewThread
import dev.chieppa.arc.core.conversation.ThreadState
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.ui.services.ConversationService
import dev.chieppa.arc.ui.services.HostedConversation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * The conversation around the workspace's sources: what the host holds of each pull request ([hosted]) and what the
 * person has written here ([local]), with the changes they make to both. Writing stays local until a push; a reaction
 * or resolving a thread goes to the host at once, shows straight away and is put back if the host refuses. Host calls
 * go through [service] on [scope]; [now] gives the time in epoch seconds.
 */
@Stable
class ConversationController(
    private val service: ConversationService,
    private val scope: CoroutineScope,
    initial: LocalConversation = LocalConversation(),
    private val now: () -> Long = { System.currentTimeMillis() / MILLIS },
) {
    var local by mutableStateOf(initial)
    var hosted by mutableStateOf<Map<Source, HostedConversation>>(emptyMap())
        private set

    /** The pull requests being read from the host. */
    var reading by mutableStateOf(emptySet<Source>())
        private set

    /** The comments and notes whose text is open for writing, by id. */
    var writing by mutableStateOf(emptySet<String>())
        private set

    /** The person's login on [source]'s host, once its conversation is read. */
    fun viewer(source: Source): String? = hosted[source]?.conversation?.viewer

    /** Reads [source]'s conversation from the host, when it is a pull request, along with the person's pending review. */
    fun load(source: Source) {
        if (source !is Source.PullRequest || source in reading) return
        reading = reading + source
        scope.launch {
            try {
                val read = service.conversation(source) ?: return@launch
                hosted = hosted + (source to read)
                local = local.withPending(source, read.conversation.pending)
            } finally {
                reading = reading - source
            }
        }
    }

    /** The host's threads on [path] of [source], each with the person's replies to it. */
    fun hostThreads(source: Source, path: String): List<ReviewThread> =
        hosted[source]?.conversation?.threads.orEmpty().filter { it.path == path }

    /** Starts a comment at [anchor] of [source] and opens it for writing; returns its id. */
    fun startComment(source: Source, anchor: CommentAnchor): String = add(LocalComment(newId(), source, LocalKind.Draft, "", now(), anchor))

    /** Opens a reply to [thread] for writing, the one being written if there is one; returns its id. */
    fun startReply(thread: ReviewThread): String {
        val open = local.comments.firstOrNull { it.inReplyTo == thread.id && it.kind == LocalKind.Draft }
        if (open != null) return open.id.also { writing = writing + it }
        val anchor = CommentAnchor(thread.path, thread.line, thread.side, thread.startLine, thread.madeOn)
        return add(LocalComment(newId(), thread.source, LocalKind.Draft, "", now(), anchor, inReplyTo = thread.id))
    }

    /** Takes a guest's note on [thread] as the person's own draft at its line, open for writing; returns its id. */
    fun adopt(thread: ReviewThread): String {
        val anchor = CommentAnchor(thread.path, thread.line, thread.side, thread.startLine, thread.madeOn)
        return add(LocalComment(newId(), thread.source, LocalKind.Draft, thread.messages.firstOrNull()?.body.orEmpty(), now(), anchor))
    }

    /** The comment on [source]'s conversation the person is writing, if there is one. */
    fun conversationDraft(source: Source): LocalComment? =
        local.of(source).firstOrNull { it.anchor == null && !it.isReply && it.kind == LocalKind.Draft }

    /** Sets the comment on [source]'s conversation being written to [body], starting one when there is none. */
    fun writeConversation(source: Source, body: String) {
        val draft = conversationDraft(source)
        if (draft == null) {
            if (body.isNotEmpty()) local = local.put(LocalComment(newId(), source, LocalKind.Draft, body, now()))
        } else {
            write(draft.id, body)
        }
    }

    fun write(id: String, body: String) {
        local = local.change(id) { it.copy(body = body) }
    }

    /** Stages comment [id] to go up with the next push; an empty one is dropped instead. */
    fun stage(id: String) = settle(id, LocalKind.Staged)

    /** Keeps comment [id] as a note on this machine; an empty one is dropped instead. */
    fun keepAsNote(id: String) = settle(id, LocalKind.Note)

    /** Makes note [id] a comment again and opens it for writing. */
    fun makeComment(id: String) = reopen(id, LocalKind.Draft)

    /** Opens [id] for writing again: a staged comment goes back to being a draft, a note stays a note. */
    fun edit(id: String) = reopen(id, if (local.byId(id)?.kind == LocalKind.Note) LocalKind.Note else LocalKind.Draft)

    /** Closes [id] for writing, keeping what it is; an empty one is dropped. */
    fun close(id: String) {
        val comment = local.byId(id) ?: return
        writing = writing - id
        if (comment.body.isBlank()) local = local.remove(id)
    }

    fun discard(id: String) {
        writing = writing - id
        local = local.remove(id)
    }

    fun summary(source: Source): ReviewSummary = local.summary(source)

    fun setSummary(source: Source, summary: ReviewSummary) {
        local = local.withSummary(source, summary)
    }

    /** The person's note on [source] as a whole. */
    fun sourceNote(source: Source): LocalComment? = local.of(source).firstOrNull { it.isNote && it.anchor == null }

    /** Sets the person's note on [source] as a whole to [body], dropping it when empty. */
    fun writeSourceNote(source: Source, body: String) {
        val note = sourceNote(source)
        local = when {
            note == null && body.isBlank() -> local
            note == null -> local.put(LocalComment(newId(), source, LocalKind.Note, body, now()))
            body.isBlank() -> local.remove(note.id)
            else -> local.change(note.id) { it.copy(body = body) }
        }
    }

    /** Toggles the person's [reaction] on the host's message [messageId] in [source]'s conversation. */
    fun react(source: Source, messageId: String, reaction: Reaction) {
        val read = hosted[source] ?: return
        val message = ConversationEdits.message(read.conversation, messageId) ?: return
        val add = !Reactions.isMine(message.reactions, reaction)
        val toggle = { conversation: HostedConversation -> conversation.copy(conversation = ConversationEdits.toggleReaction(conversation.conversation, messageId, reaction)) }
        hosted = hosted + (source to toggle(read))
        scope.launch {
            if (!service.react(source, messageId, reaction, add)) hosted[source]?.let { hosted = hosted + (source to toggle(it)) }
        }
    }

    /** Resolves [thread] on the host, or opens it again. */
    fun resolve(thread: ReviewThread, resolved: Boolean) {
        val source = thread.source
        val read = hosted[source] ?: return
        val was = thread.state
        hosted = hosted + (source to read.copy(conversation = ConversationEdits.resolve(read.conversation, thread.id, resolved)))
        scope.launch {
            if (service.resolve(source, thread.id, resolved)) return@launch
            hosted[source]?.let { now -> hosted = hosted + (source to now.copy(conversation = ConversationEdits.resolve(now.conversation, thread.id, was == ThreadState.Resolved))) }
        }
    }

    private fun add(comment: LocalComment): String {
        local = local.put(comment)
        writing = writing + comment.id
        return comment.id
    }

    private fun settle(id: String, kind: LocalKind) {
        val comment = local.byId(id) ?: return
        writing = writing - id
        local = if (comment.body.isBlank()) local.remove(id) else local.change(id) { it.copy(kind = kind, body = it.body.trim(), failed = false) }
    }

    private fun reopen(id: String, kind: LocalKind) {
        if (local.byId(id) == null) return
        local = local.change(id) { it.copy(kind = kind) }
        writing = writing + id
    }

    private fun newId(): String = UUID.randomUUID().toString()

    private companion object {
        const val MILLIS = 1000L
    }
}

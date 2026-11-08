package dev.chieppa.arc.ui.conversation

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.chieppa.arc.core.conversation.CommentRouting
import dev.chieppa.arc.core.conversation.PushItem
import dev.chieppa.arc.core.conversation.ReviewPush
import dev.chieppa.arc.core.conversation.StagedComment
import dev.chieppa.arc.core.conversation.Verdict
import dev.chieppa.arc.core.mark.FileReviewState
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.ui.review.WorkspaceState
import dev.chieppa.arc.ui.services.ConversationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Turns a source's staged comments into what a push sends, and sends it once the person confirms. Each comment's
 * route and place come from where it sits in the source now, through [placer]; a reviewed file of a pull request is
 * marked viewed on the host with the push, and a set aside one is not.
 */
@Stable
class ReviewPusher(
    private val state: WorkspaceState,
    private val conversations: ConversationController,
    private val placer: StagedPlacer,
    private val service: ConversationService,
    private val scope: CoroutineScope,
) {
    /** The source whose push is waiting for the person's confirmation. */
    var confirming by mutableStateOf<Source?>(null)

    /** The source being pushed. */
    var pushing by mutableStateOf<Source?>(null)
        private set

    /** [source]'s staged comments as they will go up, in the order they were written. */
    fun staged(source: Source): List<StagedComment> {
        val snapshot = state.snapshot(source)
        return conversations.local.staged(source).map { comment ->
            val anchor = comment.anchor
            val line = comment.anchor?.let { placer.lineOf(comment, snapshot) }
            val changed = anchor != null && snapshot?.file(anchor.path) != null
            val commentable = if (anchor != null && snapshot != null) placer.commentable(snapshot, anchor.path, anchor.side) else null
            val route = CommentRouting.route(comment, changed, commentable)
            val head = snapshot?.revisions?.head
            StagedComment(
                id = comment.id,
                source = source,
                path = anchor?.path,
                line = line ?: anchor?.line,
                body = comment.body,
                route = route,
                lineGoneIn = if (anchor != null && line == null && head != null) head.short() else null,
                movedFrom = anchor?.line?.takeIf { line != null && line != it },
                fromPending = comment.fromPending,
                failed = comment.failed,
            )
        }
    }

    /** What pushing [source] now sends. */
    fun plan(source: Source): ReviewPush {
        val snapshot = state.snapshot(source)
        val byId = conversations.local.staged(source).associateBy { it.id }
        val items = staged(source).filter { it.lineGoneIn == null }.mapNotNull { row ->
            val comment = byId[row.id] ?: return@mapNotNull null
            val anchor = comment.anchor?.let { a -> row.line?.let { a.movedTo(it, snapshot?.revisions?.head) } ?: a }
            PushItem(comment, row.route, anchor)
        }
        val viewed = if (source is Source.PullRequest) state.states(source).filterValues { it == FileReviewState.Reviewed }.keys.sorted() else emptyList()
        return ReviewPush(source, snapshot?.revisions?.head, conversations.summary(source), items, viewed)
    }

    /** How many of [source]'s files are set aside, which a push leaves unmarked on the host. */
    fun setAside(source: Source): Int = state.states(source).count { it.value == FileReviewState.SetAside }

    /** Whether [source] has anything to push: a staged comment, or for a pull request a verdict other than a plain comment or a summary. */
    fun canPush(source: Source): Boolean {
        val summary = conversations.summary(source)
        val staged = staged(source).any { it.lineGoneIn == null }
        return pushing == null && (staged || (source is Source.PullRequest && (summary.body.isNotBlank() || summary.verdict != Verdict.Comment)))
    }

    /** Sends [source]'s push, then reads its conversation again. What could not be sent stays staged, marked. */
    fun push(source: Source) {
        if (pushing != null) return
        val plan = plan(source)
        confirming = null
        pushing = source
        scope.launch {
            try {
                val outcome = service.push(plan, conversations.hosted[source])
                conversations.local = conversations.local.afterPush(source, outcome)
            } finally {
                pushing = null
            }
            conversations.load(source)
        }
    }
}

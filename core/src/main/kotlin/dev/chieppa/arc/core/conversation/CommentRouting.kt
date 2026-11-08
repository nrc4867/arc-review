package dev.chieppa.arc.core.conversation

import dev.chieppa.arc.core.source.Source

/**
 * How a staged comment goes up. A reply goes under its thread and a comment on no line goes on the conversation. On a
 * pull request, a comment on a line the host takes comments on goes on that line, one elsewhere in a changed file goes
 * on the file, and one on a file the pull request does not change goes in the review's summary. Any other source
 * takes comments on its commit.
 */
object CommentRouting {
    /**
     * The route for [comment]. [changed] is whether its file is in the source's change; [commentable] is the lines on
     * its side the host takes comments on, null when they are not known, which counts every line.
     */
    fun route(comment: LocalComment, changed: Boolean, commentable: Set<Int>?): StagedRoute {
        val anchor = comment.anchor
        return when {
            comment.isReply -> StagedRoute.Reply
            comment.source !is Source.PullRequest -> StagedRoute.Commit
            anchor == null -> StagedRoute.Conversation
            !changed -> StagedRoute.Summary
            commentable == null || ((anchor.startLine ?: anchor.line)..anchor.line).all { it in commentable } -> StagedRoute.Line
            else -> StagedRoute.File
        }
    }
}

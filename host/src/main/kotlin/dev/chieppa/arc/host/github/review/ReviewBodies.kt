package dev.chieppa.arc.host.github.review

import dev.chieppa.arc.core.conversation.CommentAnchor
import dev.chieppa.arc.core.conversation.PushItem
import dev.chieppa.arc.host.github.repo.GitHubRepo

/**
 * The Markdown sent for comments GitHub takes on no line: a file comment quotes the line and links to it, and the
 * review's summary gains a paragraph for each comment routed into it. Links are made only when the head is known.
 */
object ReviewBodies {
    private const val PARAGRAPH = "\n\n"
    private const val QUOTE = "> "
    private const val SEPARATOR = " · "
    private const val LINK_TEXT = "permalink"

    /** `path:line`, or `path:start-end` for a span. */
    fun reference(anchor: CommentAnchor): String {
        val start = anchor.startLine?.takeIf { it != anchor.line }
        return if (start != null) "${anchor.path}:$start-${anchor.line}" else "${anchor.path}:${anchor.line}"
    }

    /** The body of a thread on [anchor]'s whole file: a quote of the line reference, linked, then [body]. */
    fun file(repo: GitHubRepo, head: String?, anchor: CommentAnchor, body: String): String {
        val reference = reference(anchor)
        val quoted = link(repo, head, anchor)?.let { "[$reference]($it)" } ?: reference
        return QUOTE + quoted + PARAGRAPH + body
    }

    /** [text] followed by one paragraph per item: `**path:line** · body [permalink](url)`. */
    fun summary(repo: GitHubRepo, head: String?, text: String, items: List<PushItem>): String {
        val paragraphs = items.map { item ->
            val anchor = item.anchor ?: return@map item.comment.body
            val url = link(repo, head, anchor)
            buildString {
                append("**").append(reference(anchor)).append("**").append(SEPARATOR).append(item.comment.body)
                url?.let { append(" [").append(LINK_TEXT).append("](").append(it).append(')') }
            }
        }
        return (listOf(text).filter { it.isNotBlank() } + paragraphs).joinToString(PARAGRAPH)
    }

    private fun link(repo: GitHubRepo, head: String?, anchor: CommentAnchor): String? =
        head?.let { Permalinks.line(repo, it, anchor.path, anchor.line, anchor.startLine) }
}

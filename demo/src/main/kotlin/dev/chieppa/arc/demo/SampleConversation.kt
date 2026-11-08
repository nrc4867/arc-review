package dev.chieppa.arc.demo

import dev.chieppa.arc.core.change.ObjectId
import dev.chieppa.arc.core.conversation.LineSide
import dev.chieppa.arc.core.conversation.ReviewThread
import dev.chieppa.arc.core.conversation.ThreadContext
import dev.chieppa.arc.core.conversation.ThreadMessage
import dev.chieppa.arc.core.conversation.ThreadState
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.host.conversation.ReviewConversation

/**
 * Sample threads from other people and a guest, placed on whatever files the review shows, so the screens that draw
 * them can be seen without a pull request on GitHub. Loaded by the app only when it runs with `-Pdemo`.
 */
class SampleConversation(private val now: () -> Long = { System.currentTimeMillis() / MILLIS }) : ReviewConversation {
    override fun threads(source: Source, path: String, context: ThreadContext): List<ReviewThread> {
        val changed = context.changedLines.distinct().sorted()
        if (changed.isEmpty()) return emptyList()
        val at = SampleLines.spread(changed, context.lineCount)
        fun thread(kind: String, line: Int?, state: ThreadState, messages: List<ThreadMessage>, changedIn: String? = null, link: String? = null) =
            line?.let { ReviewThread("$kind:${source.ref}:$path", source, path, it, LineSide.New, state, messages, changedIn, link) }
        val time = now()
        return listOfNotNull(
            thread("resolved", at.resolved, ThreadState.Resolved, listOf(message(Samples.JPARK, Samples.RESOLVED_ASK, time - Samples.DAY), message(Samples.AUTHOR, Samples.RESOLVED_DONE, time - Samples.HOURS_5))),
            thread("open", at.open, ThreadState.Open, listOf(message(Samples.MFISCHER, Samples.OPEN_ASK, time - Samples.HOURS_2), message(Samples.AUTHOR, Samples.OPEN_REPLY, time - Samples.HOUR)), link = Samples.LINK),
            thread("guest", at.guest, ThreadState.Guest, listOf(ThreadMessage(Samples.GUEST, Samples.GUEST_NOTE, time - Samples.MINUTES_4, guest = true))),
            thread("addressed", at.addressed, ThreadState.Addressed, listOf(message(Samples.DANA, Samples.ADDRESSED, time - Samples.DAY)), changedIn = Samples.CHANGED_IN),
        )
    }


    override fun revisionThreads(source: Source, paths: List<String>, heads: List<ObjectId>): List<ReviewThread> =
        SampleRevisionThreads.place(source, paths, heads, now())

    private fun message(author: String, body: String, at: Long) = ThreadMessage(author, body, at)

    private companion object {
        const val MILLIS = 1000L
    }
}

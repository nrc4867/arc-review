package dev.chieppa.arc.demo

import dev.chieppa.arc.core.conversation.LineSide
import dev.chieppa.arc.core.conversation.ReviewThread
import dev.chieppa.arc.core.conversation.StagedComment
import dev.chieppa.arc.core.conversation.StagedRoute
import dev.chieppa.arc.core.conversation.ThreadContext
import dev.chieppa.arc.core.conversation.ThreadMessage
import dev.chieppa.arc.core.conversation.ThreadState
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.host.conversation.ReviewConversation

/**
 * Sample threads and staged comments, placed on whatever files the review shows, so the screens that draw them can be
 * seen before the store and the GitHub host provide real ones. Loaded by the app only when it runs with `-Pdemo`.
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
            thread("draft", at.draft, ThreadState.Draft, listOf(message(Samples.YOU, Samples.DRAFT, time))),
        )
    }

    override fun staged(changes: Map<Source, List<String>>): List<StagedComment> {
        val sources = changes.filterValues { it.isNotEmpty() }
        val first = sources.entries.firstOrNull() ?: return emptyList()
        val files = first.value
        fun file(i: Int) = files[i % files.size]
        val rows = mutableListOf(
            StagedComment("s1", first.key, file(0), Samples.LINE_51, Samples.STAGED_METRIC),
            StagedComment("s2", first.key, file(1), Samples.LINE_14, Samples.STAGED_INDEX),
            StagedComment("s3", first.key, file(2), Samples.LINE_31, Samples.STAGED_GONE, lineGoneIn = Samples.CHANGED_IN),
            StagedComment("s4", first.key, file(3), Samples.LINE_22, Samples.STAGED_SUMMARY, route = StagedRoute.Summary),
        )
        sources.entries.drop(1).firstOrNull()?.let { (source, paths) -> rows += StagedComment("s5", source, paths.first(), Samples.LINE_40, Samples.STAGED_RETRY) }
        return rows
    }

    private fun message(author: String, body: String, at: Long) = ThreadMessage(author, body, at)

    private companion object {
        const val MILLIS = 1000L
    }
}

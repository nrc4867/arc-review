package dev.chieppa.arc.demo

import dev.chieppa.arc.core.change.ObjectId
import dev.chieppa.arc.core.conversation.LineSide
import dev.chieppa.arc.core.conversation.ReviewThread
import dev.chieppa.arc.core.conversation.ThreadMessage
import dev.chieppa.arc.core.conversation.ThreadState
import dev.chieppa.arc.core.revision.TreePathOrder
import dev.chieppa.arc.core.source.Source

/**
 * Sample threads placed on a source's revisions the way the revision matrix's board draws them: on the first file, a
 * resolved thread on the first revision, an open one halfway and an open and an addressed one on the last; an
 * addressed thread on the third file halfway; a resolved one on the sixth file's first revision, an open one on the
 * seventh's, and a resolved one on the tenth file's last but one. Files the source does not have are skipped.
 */
internal object SampleRevisionThreads {
    private enum class At { First, Middle, LastButOne, Last }

    private data class Placement(val file: Int, val at: At, val state: ThreadState)

    private val PLACEMENTS = listOf(
        Placement(0, At.First, ThreadState.Resolved),
        Placement(0, At.Middle, ThreadState.Open),
        Placement(0, At.Last, ThreadState.Open),
        Placement(0, At.Last, ThreadState.Addressed),
        Placement(2, At.Middle, ThreadState.Addressed),
        Placement(5, At.First, ThreadState.Resolved),
        Placement(6, At.First, ThreadState.Open),
        Placement(9, At.LastButOne, ThreadState.Resolved),
    )
    private const val LINE = 1

    fun place(source: Source, paths: List<String>, heads: List<ObjectId>, time: Long): List<ReviewThread> {
        if (heads.isEmpty()) return emptyList()
        val files = paths.distinct().sortedWith(TreePathOrder)
        return PLACEMENTS.mapIndexedNotNull { i, placement ->
            val path = files.getOrNull(placement.file) ?: return@mapIndexedNotNull null
            val head = heads[index(placement.at, heads.size)]
            ReviewThread(
                id = "revision:$i:${source.ref}:$path",
                source = source,
                path = path,
                line = LINE,
                side = LineSide.New,
                state = placement.state,
                messages = listOf(message(placement.state, time)),
                madeOn = head,
            )
        }
    }

    private fun index(at: At, count: Int): Int = when (at) {
        At.First -> 0
        At.Middle -> count / 2
        At.LastButOne -> (count - 2).coerceAtLeast(0)
        At.Last -> count - 1
    }

    private fun message(state: ThreadState, time: Long): ThreadMessage = when (state) {
        ThreadState.Resolved -> ThreadMessage(Samples.JPARK, Samples.RESOLVED_ASK, time - Samples.DAY)
        ThreadState.Addressed -> ThreadMessage(Samples.DANA, Samples.ADDRESSED, time - Samples.DAY)
        else -> ThreadMessage(Samples.MFISCHER, Samples.OPEN_ASK, time - Samples.HOURS_2)
    }
}

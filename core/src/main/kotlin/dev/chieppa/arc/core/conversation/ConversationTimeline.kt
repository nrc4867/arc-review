package dev.chieppa.arc.core.conversation

/** A pull request's conversation laid out as items to read, either in time order or by file. */
object ConversationTimeline {
    /**
     * The description, then the comments and reviews oldest first, each review holding the threads it started. A
     * thread whose review is not among the entries stands on its own at the time of its first message.
     */
    fun inOrder(conversation: PullConversation): List<TimelineItem> {
        val reviews = conversation.entries.filterIsInstance<ConversationEntry.Review>().map { it.message.id }.toSet()
        val byReview = conversation.threads.groupBy { it.messages.firstOrNull()?.review?.takeIf(reviews::contains) }
        val entries = conversation.entries.map { entry ->
            val threads = if (entry is ConversationEntry.Review) byReview[entry.message.id].orEmpty().sortedWith(IN_FILE) else emptyList()
            TimelineItem.Entry(entry, threads)
        }
        val alone = byReview[null].orEmpty().map(TimelineItem::Thread)
        val items = (entries + alone).sortedBy { at(it) }
        return listOfNotNull(conversation.description?.let(TimelineItem::Description)) + items
    }

    /** The description, then the threads grouped by file, files in path order and threads in line order. */
    fun byFile(conversation: PullConversation): List<TimelineItem> =
        listOfNotNull(conversation.description?.let(TimelineItem::Description)) +
            conversation.threads.groupBy { it.path }.toSortedMap().map { (path, threads) -> TimelineItem.File(path, threads.sortedWith(IN_FILE)) }

    private fun at(item: TimelineItem): Long = when (item) {
        is TimelineItem.Entry -> item.entry.message.writtenAt
        is TimelineItem.Thread -> item.thread.messages.firstOrNull()?.writtenAt ?: 0
        is TimelineItem.Description -> item.message.writtenAt
        is TimelineItem.File -> 0
    }

    private val IN_FILE = compareBy<ReviewThread>({ it.path }, { it.line })
}

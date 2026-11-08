package dev.chieppa.arc.core.conversation

/** One item of a pull request's conversation as it reads top to bottom. [key] tells items apart across reads. */
sealed interface TimelineItem {
    val key: String

    /** The pull request's description. */
    data class Description(val message: ThreadMessage) : TimelineItem {
        override val key: String get() = "description"
    }

    /** A comment on the pull request, or a submitted review with the [threads] it started. */
    data class Entry(val entry: ConversationEntry, val threads: List<ReviewThread> = emptyList()) : TimelineItem {
        override val key: String get() = entry.message.id.ifEmpty { "entry-${entry.message.writtenAt}" }
    }

    /** A thread whose review is not in the conversation, at the time it was started. */
    data class Thread(val thread: ReviewThread) : TimelineItem {
        override val key: String get() = thread.id
    }

    /** The threads on one file, in line order, when threads are grouped by file. */
    data class File(val path: String, val threads: List<ReviewThread>) : TimelineItem {
        override val key: String get() = "file:$path"
    }
}

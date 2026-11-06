package dev.chieppa.arc.core.conversation

/** One message in a thread: who wrote it, whether that was an outside agent, when (epoch seconds) and what. */
data class ThreadMessage(
    val author: String,
    val body: String,
    val writtenAt: Long,
    val guest: Boolean = false,
)

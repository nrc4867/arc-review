package dev.chieppa.arc.core.conversation

/**
 * One message in a thread or a pull request's conversation: who wrote it, whether that was an outside agent, when
 * (epoch seconds) and what. [id] is the host's id for it, which reactions are left on; [avatar] is the address of its
 * author's picture, where the host gives one.
 */
data class ThreadMessage(
    val author: String,
    val body: String,
    val writtenAt: Long,
    val guest: Boolean = false,
    val id: String = "",
    val avatar: String? = null,
    val reactions: List<ReactionGroup> = emptyList(),
)

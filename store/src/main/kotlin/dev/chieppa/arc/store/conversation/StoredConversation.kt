package dev.chieppa.arc.store.conversation

import kotlinx.serialization.Serializable

/** The person's drafts, staged comments, notes and each source's verdict and summary, as written. */
@Serializable
data class StoredConversation(
    val comments: List<StoredComment> = emptyList(),
    val summaries: List<StoredSummary> = emptyList(),
)

/** One draft, staged comment or note: its source ref, kind name, text, when it was written and where it sits. */
@Serializable
data class StoredComment(
    val id: String,
    val source: String,
    val kind: String,
    val body: String,
    val writtenAt: Long,
    val path: String? = null,
    val line: Int? = null,
    val side: String? = null,
    val startLine: Int? = null,
    val madeOn: String? = null,
    val inReplyTo: String? = null,
    val failed: Boolean = false,
)

/** A source's verdict name and summary text. */
@Serializable
data class StoredSummary(val source: String, val verdict: String, val body: String = "")

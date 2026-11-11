package dev.chieppa.arc.plugins.protocol

import kotlinx.serialization.Serializable

/**
 * One of the person's notes, or a comment draft, as a plugin sees it: its [id], [kind], the [source] ref and [repo] it
 * is on, the [path] and [line] (from [startLine] for a range) on [side] it sits at when it is on lines, the [revision]
 * it was written at, its [text], and when it was [createdAt] and last [updatedAt], in epoch milliseconds.
 */
@Serializable
data class NoteInfo(
    val id: String,
    val kind: NoteKind,
    val source: String,
    val repo: RepoInfo,
    val text: String,
    val createdAt: Long,
    val updatedAt: Long,
    val path: String? = null,
    val line: Int? = null,
    val startLine: Int? = null,
    val side: NoteSide? = null,
    val revision: String? = null,
)

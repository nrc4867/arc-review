package dev.chieppa.arc.core.change

/** One commit as a list of commits shows it: its id, first line, author and when it was made (epoch seconds). */
data class CommitInfo(
    val id: ObjectId,
    val subject: String,
    val author: String,
    val time: Long,
)

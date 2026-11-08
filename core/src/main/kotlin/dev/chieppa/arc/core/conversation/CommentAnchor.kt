package dev.chieppa.arc.core.conversation

import dev.chieppa.arc.core.change.ObjectId

/**
 * Where a comment or note sits: a line of [path] on one [side], or the lines from [startLine] to [line]. [madeOn] is
 * the source's head when it was written, so it can be moved with the lines when the source moves.
 */
data class CommentAnchor(
    val path: String,
    val line: Int,
    val side: LineSide = LineSide.New,
    val startLine: Int? = null,
    val madeOn: ObjectId? = null,
) {
    /** This anchor moved to [line], keeping its span, at the head [on]. */
    fun movedTo(line: Int, on: ObjectId?): CommentAnchor =
        copy(line = line, startLine = startLine?.let { line - (this.line - it) }, madeOn = on ?: madeOn)
}

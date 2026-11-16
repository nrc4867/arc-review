package dev.chieppa.arc.syntax.runtime

import dev.chieppa.arc.syntax.engine.CaptureLayout

/**
 * Every capture of one query run, flat: for capture [i], the match it belongs to (numbered from 0 in order), the
 * query pattern that matched, which capture of the query it is, its range as string indices, and how many named
 * children the captured node has. Captures of one match are consecutive.
 */
class QueryRecords internal constructor(private val data: IntArray) {
    val size: Int get() = data.size / STRIDE

    fun match(i: Int): Int = data[STRIDE * i + CaptureLayout.MATCH]
    fun pattern(i: Int): Int = data[STRIDE * i + CaptureLayout.PATTERN]
    fun capture(i: Int): Int = data[STRIDE * i + CaptureLayout.CAPTURE]
    fun start(i: Int): Int = data[STRIDE * i + CaptureLayout.START] / Char.SIZE_BYTES
    fun end(i: Int): Int = data[STRIDE * i + CaptureLayout.END] / Char.SIZE_BYTES
    fun namedChildren(i: Int): Int = data[STRIDE * i + CaptureLayout.CHILDREN]

    /** Calls [block] with the first and last-plus-one record index of each match, in order. */
    inline fun forEachMatch(block: (from: Int, until: Int) -> Unit) {
        var i = 0
        while (i < size) {
            val match = match(i)
            var j = i + 1
            while (j < size && match(j) == match) j++
            block(i, j)
            i = j
        }
    }

    /** These records with only the matches [keep] accepts, given each match's record range. */
    internal fun filterMatches(keep: (from: Int, until: Int) -> Boolean): QueryRecords {
        val kept = IntArray(data.size)
        var length = 0
        forEachMatch { from, until ->
            if (keep(from, until)) {
                System.arraycopy(data, STRIDE * from, kept, length, STRIDE * (until - from))
                length += STRIDE * (until - from)
            }
        }
        return QueryRecords(kept.copyOf(length))
    }

    private companion object {
        const val STRIDE = CaptureLayout.STRIDE
    }
}

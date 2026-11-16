package dev.chieppa.arc.syntax.engine

/**
 * The record `arc_query_records` (`tree-sitter/arc_shim.c`) writes per capture, [STRIDE] ints: the match number (from
 * 0 in order), the pattern index, the capture index, the start and end byte in the UTF-16 text, and how many named
 * children the captured node has. Captures of one match are consecutive.
 */
object CaptureLayout {
    const val STRIDE = 6
    const val MATCH = 0
    const val PATTERN = 1
    const val CAPTURE = 2
    const val START = 3
    const val END = 4
    const val CHILDREN = 5

    /** The bytes of one record. */
    const val BYTES = STRIDE * Int.SIZE_BYTES
}

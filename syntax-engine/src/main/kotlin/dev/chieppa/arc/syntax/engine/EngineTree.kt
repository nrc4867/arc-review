package dev.chieppa.arc.syntax.engine

/** A parsed text, parsed as UTF-16 little-endian. Close it when done. */
interface EngineTree : AutoCloseable {
    /** Every capture of every match of [query] over the whole tree, as [CaptureLayout] records, before predicates. */
    fun captures(query: EngineQuery): IntArray
}

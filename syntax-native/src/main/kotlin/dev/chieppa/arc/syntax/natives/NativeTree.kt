package dev.chieppa.arc.syntax.natives

import dev.chieppa.arc.syntax.engine.CaptureLayout
import dev.chieppa.arc.syntax.engine.EngineQuery
import dev.chieppa.arc.syntax.engine.EngineTree
import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout

/** A tree in native memory, freed when closed. */
internal class NativeTree(private val grammar: String, private val pointer: MemorySegment) : EngineTree {
    private var closed = false

    override fun captures(query: EngineQuery): IntArray {
        check(!closed) { "tree is closed" }
        require(query is NativeQuery && query.grammar == grammar) { "query is not for $grammar" }
        var capacity = INITIAL_RECORDS
        Arena.ofConfined().use { arena ->
            while (true) {
                val buffer = arena.allocate(ValueLayout.JAVA_INT, CaptureLayout.STRIDE.toLong() * capacity)
                val count = TreeSitterApi.queryRecords.invokeWithArguments(pointer, query.pointer, buffer, capacity) as Int
                if (count <= capacity) return buffer.asSlice(0, CaptureLayout.BYTES.toLong() * count).toArray(ValueLayout.JAVA_INT)
                capacity = count
            }
        }
    }

    override fun close() {
        if (closed) return
        closed = true
        TreeSitterApi.treeDelete.invokeWithArguments(pointer)
    }

    private companion object {
        const val INITIAL_RECORDS = 1 shl 14
    }
}

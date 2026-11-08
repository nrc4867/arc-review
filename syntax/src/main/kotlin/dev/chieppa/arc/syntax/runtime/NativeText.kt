package dev.chieppa.arc.syntax.runtime

import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout

/** Strings tree-sitter hands back as a pointer and a length. */
internal object NativeText {
    fun captureName(query: MemorySegment, id: Int): String = read { length -> TreeSitterApi.queryCaptureName.invokeWithArguments(query, id, length) as MemorySegment }

    fun queryString(query: MemorySegment, id: Int): String = read { length -> TreeSitterApi.queryStringValue.invokeWithArguments(query, id, length) as MemorySegment }

    private fun read(call: (MemorySegment) -> MemorySegment): String = Arena.ofConfined().use { arena ->
        val length = arena.allocate(ValueLayout.JAVA_INT)
        val pointer = call(length)
        val bytes = length.get(ValueLayout.JAVA_INT, 0)
        String(pointer.reinterpret(bytes.toLong()).toArray(ValueLayout.JAVA_BYTE), Charsets.UTF_8)
    }
}

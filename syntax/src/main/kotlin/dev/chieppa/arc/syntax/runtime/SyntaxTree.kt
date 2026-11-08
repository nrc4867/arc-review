package dev.chieppa.arc.syntax.runtime

import dev.chieppa.arc.syntax.grammar.Grammar
import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout
import java.nio.ByteOrder

/**
 * A parsed [text] in [grammar]. It holds native memory: close it when done. Positions in it are string indices into
 * [text], since the text is parsed as UTF-16.
 */
class SyntaxTree private constructor(val grammar: Grammar, val text: String, internal val pointer: MemorySegment) : AutoCloseable {
    private var closed = false

    override fun close() {
        if (closed) return
        closed = true
        TreeSitterApi.treeDelete.invokeWithArguments(pointer)
    }

    companion object {
        private val UTF16LE = ValueLayout.JAVA_CHAR_UNALIGNED.withOrder(ByteOrder.LITTLE_ENDIAN)

        /** Parses [text] as [grammar]. */
        fun parse(grammar: Grammar, text: String): SyntaxTree {
            val pointer = Arena.ofConfined().use { arena ->
                val input = arena.allocate(text.length.toLong() * Char.SIZE_BYTES)
                MemorySegment.copy(text.toCharArray(), 0, input, UTF16LE, 0, text.length)
                ParserPool.withParser(grammar) { parser ->
                    TreeSitterApi.parserParseStringEncoding.invokeWithArguments(
                        parser, MemorySegment.NULL, input, text.length * Char.SIZE_BYTES, TreeSitterApi.ENCODING_UTF16LE,
                    ) as MemorySegment
                }
            }
            check(pointer != MemorySegment.NULL) { "tree-sitter gave no tree for ${grammar.id}" }
            return SyntaxTree(grammar, text, pointer)
        }
    }
}

package dev.chieppa.arc.syntax.natives

import dev.chieppa.arc.syntax.engine.EngineQuery
import dev.chieppa.arc.syntax.engine.EngineTree
import dev.chieppa.arc.syntax.engine.SyntaxEngine
import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout
import java.nio.ByteOrder

/**
 * tree-sitter's native libraries through Arc's own FFM bindings, for comparison with the WebAssembly engine; on the
 * classpath only with `-Psyntax.engine=native`. Found by `SyntaxEngines` through `META-INF/services`.
 */
class NativeSyntaxEngine : SyntaxEngine {
    override fun parse(grammar: String, text: String): EngineTree {
        val pointer = Arena.ofConfined().use { arena ->
            val input = arena.allocate(text.length.toLong() * Char.SIZE_BYTES)
            MemorySegment.copy(text.toCharArray(), 0, input, UTF16LE, 0, text.length)
            ParserPool.withParser(grammar) { parser ->
                TreeSitterApi.parserParseStringEncoding.invokeWithArguments(
                    parser, MemorySegment.NULL, input, text.length * Char.SIZE_BYTES, TreeSitterApi.ENCODING_UTF16LE,
                ) as MemorySegment
            }
        }
        check(pointer != MemorySegment.NULL) { "tree-sitter gave no tree for $grammar" }
        return NativeTree(grammar, pointer)
    }

    override fun compile(grammar: String, source: String): EngineQuery = NativeQuery.compile(grammar, source)

    private companion object {
        val UTF16LE: ValueLayout.OfChar = ValueLayout.JAVA_CHAR_UNALIGNED.withOrder(ByteOrder.LITTLE_ENDIAN)
    }
}

package dev.chieppa.arc.syntax.natives

import java.lang.foreign.MemorySegment
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Parsers set to each grammar, lent out one per parse. A parser is not thread-safe, so each parse takes one of its own
 * and gives it back; the pool grows to as many parsers as parses ever ran at once.
 */
internal object ParserPool {
    private val idle = ConcurrentHashMap<String, ConcurrentLinkedQueue<MemorySegment>>()

    fun <T> withParser(grammar: String, block: (MemorySegment) -> T): T {
        val queue = idle.computeIfAbsent(grammar) { ConcurrentLinkedQueue() }
        val parser = queue.poll() ?: create(grammar)
        try {
            return block(parser)
        } finally {
            queue.offer(parser)
        }
    }

    private fun create(grammar: String): MemorySegment {
        val parser = TreeSitterApi.parserNew.invokeWithArguments() as MemorySegment
        val set = TreeSitterApi.parserSetLanguage.invokeWithArguments(parser, SyntaxLanguages.of(grammar)) as Boolean
        check(set) { "tree-sitter rejected the $grammar language's version" }
        return parser
    }
}

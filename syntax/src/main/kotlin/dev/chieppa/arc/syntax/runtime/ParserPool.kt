package dev.chieppa.arc.syntax.runtime

import dev.chieppa.arc.syntax.grammar.Grammar
import java.lang.foreign.MemorySegment
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Parsers set to each grammar, lent out one per parse. A parser is not thread-safe, so each parse takes one of its own
 * and gives it back; the pool grows to as many parsers as parses ever ran at once.
 */
internal object ParserPool {
    private val idle = ConcurrentHashMap<String, ConcurrentLinkedQueue<MemorySegment>>()

    fun <T> withParser(grammar: Grammar, block: (MemorySegment) -> T): T {
        val queue = idle.computeIfAbsent(grammar.id) { ConcurrentLinkedQueue() }
        val parser = queue.poll() ?: create(grammar)
        try {
            return block(parser)
        } finally {
            queue.offer(parser)
        }
    }

    fun create(grammar: Grammar): MemorySegment {
        val parser = TreeSitterApi.parserNew.invokeWithArguments() as MemorySegment
        val set = TreeSitterApi.parserSetLanguage.invokeWithArguments(parser, SyntaxLanguages.of(grammar)) as Boolean
        check(set) { "tree-sitter rejected the ${grammar.id} language's version" }
        return parser
    }
}

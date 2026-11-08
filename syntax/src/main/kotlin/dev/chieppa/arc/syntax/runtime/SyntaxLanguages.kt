package dev.chieppa.arc.syntax.runtime

import dev.chieppa.arc.syntax.grammar.Grammar
import java.lang.foreign.MemorySegment
import java.util.concurrent.ConcurrentHashMap

/** Each grammar's loaded language, loaded once and kept for the life of the process. A language is immutable. */
internal object SyntaxLanguages {
    private val loaded = ConcurrentHashMap<String, MemorySegment>()

    fun of(grammar: Grammar): MemorySegment = loaded.computeIfAbsent(grammar.id) { TreeSitterApi.language(grammar.library, grammar.symbol) }
}

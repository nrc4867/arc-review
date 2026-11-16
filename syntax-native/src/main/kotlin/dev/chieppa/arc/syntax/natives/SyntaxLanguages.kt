package dev.chieppa.arc.syntax.natives

import java.lang.foreign.MemorySegment
import java.util.concurrent.ConcurrentHashMap

/**
 * Each grammar's loaded language, loaded once and kept for the life of the process. A language is immutable. Grammar
 * `<id>` is the library `tree-sitter-<id>` with the entry `tree_sitter_<id>`.
 */
internal object SyntaxLanguages {
    private val loaded = ConcurrentHashMap<String, MemorySegment>()

    fun of(grammar: String): MemorySegment = loaded.computeIfAbsent(grammar) { TreeSitterApi.language(library(it), symbol(it)) }

    fun library(grammar: String): String = "${NativeLibraries.CORE}-$grammar"

    fun symbol(grammar: String): String = "tree_sitter_$grammar"
}

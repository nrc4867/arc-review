package dev.chieppa.arc.syntax.runtime

import dev.chieppa.arc.syntax.engine.EngineTree
import dev.chieppa.arc.syntax.engine.SyntaxEngines
import dev.chieppa.arc.syntax.grammar.Grammar

/**
 * A parsed [text] in [grammar], held by the syntax engine until closed. Positions in it are string indices into
 * [text], since the text is parsed as UTF-16.
 */
class SyntaxTree private constructor(val grammar: Grammar, val text: String, internal val tree: EngineTree) : AutoCloseable {
    override fun close() {
        tree.close()
    }

    companion object {
        /** Parses [text] as [grammar]. */
        fun parse(grammar: Grammar, text: String): SyntaxTree = SyntaxTree(grammar, text, SyntaxEngines.current.parse(grammar.id, text))
    }
}

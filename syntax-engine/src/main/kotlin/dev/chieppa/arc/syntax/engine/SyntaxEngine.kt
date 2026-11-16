package dev.chieppa.arc.syntax.engine

/**
 * A tree-sitter runtime with Arc's grammars, named by grammar id (`java`, `kotlin`, `javascript`, `typescript`, `tsx`,
 * `mssql`). Implementations are thread-safe; each tree they give is used by one thread at a time.
 */
interface SyntaxEngine {
    /** Parses [text] with [grammar]. The tree holds the engine's memory until closed. */
    fun parse(grammar: String, text: String): EngineTree

    /** Compiles query [source] for [grammar]; throws [QueryException] naming where it fails. */
    fun compile(grammar: String, source: String): EngineQuery
}

package dev.chieppa.arc.syntax.wasm

import dev.chieppa.arc.syntax.engine.EngineQuery
import dev.chieppa.arc.syntax.engine.EngineTree

/** A tree in [runtime]'s memory, which it holds until closed and then gives back to [pool]. */
internal class WasmTree(private val pool: GrammarPool, private val runtime: WasmRuntime, private val tree: Long) : EngineTree {
    private var closed = false

    override fun captures(query: EngineQuery): IntArray {
        check(!closed) { "tree is closed" }
        require(query is WasmQuery && query.grammar == runtime.grammar.id) { "query is not for ${runtime.grammar.id}" }
        return runtime.captures(tree, query.index) { pool.source(query.index) }
    }

    override fun close() {
        if (closed) return
        closed = true
        runtime.deleteTree(tree)
        pool.giveBack(runtime)
    }
}

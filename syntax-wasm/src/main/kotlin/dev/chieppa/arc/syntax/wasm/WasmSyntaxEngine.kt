package dev.chieppa.arc.syntax.wasm

import dev.chieppa.arc.syntax.engine.EngineQuery
import dev.chieppa.arc.syntax.engine.EngineTree
import dev.chieppa.arc.syntax.engine.SyntaxEngine
import java.util.concurrent.ConcurrentHashMap

/**
 * tree-sitter compiled to WebAssembly and from there to JVM classes at build time, run by Chicory: no native code,
 * nothing interpreted or generated at runtime. Found by `SyntaxEngines` through `META-INF/services`.
 */
class WasmSyntaxEngine : SyntaxEngine {
    private val pools = ConcurrentHashMap<String, GrammarPool>()

    private fun pool(grammar: String) = pools.computeIfAbsent(grammar) { GrammarPool(WasmGrammars.of(it)) }

    override fun parse(grammar: String, text: String): EngineTree {
        val pool = pool(grammar)
        val runtime = pool.lend()
        val tree = try {
            runtime.parse(text)
        } catch (failure: RuntimeException) {
            pool.giveBack(runtime)
            throw failure
        }
        return WasmTree(pool, runtime, tree)
    }

    override fun compile(grammar: String, source: String): EngineQuery = pool(grammar).compile(source)
}

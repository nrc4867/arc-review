package dev.chieppa.arc.syntax.wasm

import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.CopyOnWriteArrayList

/**
 * One grammar's instances. A template instance, never lent, starts from the grammar's [Snapshot] (Arc's queries
 * compiled at build time) and compiles any other query the grammar is asked for; each instance lent to a parse is a
 * copy of it, so a new one starts with every query compiled so far. Instances go back to the pool after their parse,
 * unless their memory grew past [WasmLimits.POOLED_MEMORY_BYTES]; the pool grows to as many as parses ever ran at once.
 */
internal class GrammarPool(private val grammar: WasmGrammar) {
    private val template: WasmRuntime by lazy {
        Snapshot.bundled(grammar.id)?.let { WasmRuntime.restore(grammar, it) } ?: WasmRuntime.start(grammar)
    }
    private val idle = ConcurrentLinkedQueue<WasmRuntime>()
    private val sources = CopyOnWriteArrayList<String>()

    /** Takes [source] as a query of this grammar, compiled in the template, under its index in this grammar. */
    fun compile(source: String): WasmQuery = synchronized(this) {
        val index = sources.size
        val shape = template.describe(index, source)
        sources += source
        WasmQuery(grammar.id, index, shape.captureNames, shape.predicates)
    }

    /** The source of query [index], for an instance copied before it was compiled. */
    fun source(index: Int): String = sources[index]

    fun lend(): WasmRuntime = idle.poll() ?: synchronized(this) { WasmRuntime.copyOf(template) }

    fun giveBack(runtime: WasmRuntime) {
        if (runtime.memoryBytes <= WasmLimits.POOLED_MEMORY_BYTES) idle.offer(runtime)
    }
}

package dev.chieppa.arc.syntax.wasm

import com.dylibso.chicory.runtime.ByteArrayMemory
import com.dylibso.chicory.runtime.Instance
import com.dylibso.chicory.runtime.Machine
import com.dylibso.chicory.wasm.Parser
import com.dylibso.chicory.wasm.WasmModule

/**
 * One grammar's module: tree-sitter's runtime, the shim and the grammar, compiled at build time to [machine]'s
 * classes, with the module's memory, data and tables read from its `.meta` resource [meta].
 */
internal class WasmGrammar(val id: String, private val meta: String, private val machine: (Instance) -> Machine) {
    private val module: WasmModule by lazy {
        val stream = WasmGrammar::class.java.getResourceAsStream(meta) ?: error("No $meta bundled")
        stream.use { Parser.parse(it) }
    }

    /** A fresh instance of the module, before its `_initialize`. */
    fun instantiate(): Instance = Instance.builder(module)
        .withImportValues(WasiStubs.imports)
        .withMachineFactory(machine)
        .withMemoryFactory(::ByteArrayMemory)
        .build()

    /** The grammar's language entry point. */
    val entry: String get() = "tree_sitter_$id"
}

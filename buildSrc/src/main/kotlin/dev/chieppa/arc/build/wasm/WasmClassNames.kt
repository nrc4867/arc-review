package dev.chieppa.arc.build.wasm

/** The class name a grammar's compiled module takes: its name capitalised, so `kotlin` gives `KotlinMachine`. */
object WasmClassNames {
    fun of(grammar: String): String = grammar.replaceFirstChar { it.uppercaseChar() }
}

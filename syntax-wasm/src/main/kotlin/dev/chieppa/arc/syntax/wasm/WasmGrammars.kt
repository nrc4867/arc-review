package dev.chieppa.arc.syntax.wasm

import dev.chieppa.arc.syntax.wasm.generated.JavaMachine
import dev.chieppa.arc.syntax.wasm.generated.JavascriptMachine
import dev.chieppa.arc.syntax.wasm.generated.KotlinMachine
import dev.chieppa.arc.syntax.wasm.generated.MssqlMachine
import dev.chieppa.arc.syntax.wasm.generated.TsxMachine
import dev.chieppa.arc.syntax.wasm.generated.TypescriptMachine

/** The grammars `:syntax-wasm:compileWasmClasses` compiles, one per line of `grammars.tsv`, by grammar id. */
internal object WasmGrammars {
    private const val ROOT = "/dev/chieppa/arc/syntax/wasm/generated"

    private val all = listOf(
        WasmGrammar("java", "$ROOT/Java.meta") { JavaMachine(it) },
        WasmGrammar("kotlin", "$ROOT/Kotlin.meta") { KotlinMachine(it) },
        WasmGrammar("javascript", "$ROOT/Javascript.meta") { JavascriptMachine(it) },
        WasmGrammar("typescript", "$ROOT/Typescript.meta") { TypescriptMachine(it) },
        WasmGrammar("tsx", "$ROOT/Tsx.meta") { TsxMachine(it) },
        WasmGrammar("mssql", "$ROOT/Mssql.meta") { MssqlMachine(it) },
    ).associateBy { it.id }

    val ids: Set<String> get() = all.keys

    fun of(id: String): WasmGrammar = all[id] ?: throw IllegalArgumentException("No WebAssembly grammar $id")
}

package dev.chieppa.arc.syntax.wasm

import dev.chieppa.arc.syntax.engine.CaptureLayout
import dev.chieppa.arc.syntax.engine.PredicateStep
import dev.chieppa.arc.syntax.engine.QueryException
import dev.chieppa.arc.syntax.engine.SyntaxEngines
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotNull

class WasmSyntaxEngineTest {
    private val engine = WasmSyntaxEngine()

    @Test
    fun isTheEngineOnTheClasspath() {
        assertIs<WasmSyntaxEngine>(SyntaxEngines.current)
    }

    @Test
    fun aQueryTheBuildDidNotCompileCompilesAndCapturesByUtf16Offsets() {
        val text = "class Ä { void run() {} }"
        val query = engine.compile("java", "(method_declaration name: (identifier) @name (#eq? @name \"run\"))")
        assertEquals(listOf("name"), query.captureNames)
        assertEquals(PredicateStep.Text("eq?"), query.predicates(0).first())
        val records = engine.parse("java", text).use { it.captures(query) }
        assertEquals(CaptureLayout.STRIDE, records.size)
        val start = records[CaptureLayout.START] / Char.SIZE_BYTES
        val end = records[CaptureLayout.END] / Char.SIZE_BYTES
        assertEquals("run", text.substring(start, end))
    }

    @Test
    fun aQueryThatDoesNotCompileNamesItsLine() {
        val failure = assertFailsWith<QueryException> { engine.compile("java", "(identifier) @name\n(no_such_node) @x") }
        assertEquals(true, failure.message?.contains("line 2"))
    }

    @Test
    fun everyGrammarHasASnapshotThatRestores() {
        for (id in WasmGrammars.ids) {
            val snapshot = assertNotNull(Snapshot.bundled(id), id)
            val runtime = WasmRuntime.restore(WasmGrammars.of(id), snapshot)
            runtime.deleteTree(runtime.parse("x"))
        }
    }
}

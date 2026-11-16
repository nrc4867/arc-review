package dev.chieppa.arc.syntax.wasm

import dev.chieppa.arc.syntax.engine.EngineQuery
import dev.chieppa.arc.syntax.engine.PredicateStep

/** A query of grammar [grammar], compiled into every instance of it under [index]. */
internal class WasmQuery(
    val grammar: String,
    val index: Int,
    override val captureNames: List<String>,
    private val steps: List<List<PredicateStep>>,
) : EngineQuery {
    override val patternCount: Int get() = steps.size

    override fun predicates(pattern: Int): List<PredicateStep> = steps[pattern]
}

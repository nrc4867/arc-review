package dev.chieppa.arc.syntax.facts

import dev.chieppa.arc.syntax.grammar.Grammars

/** The small source files under `src/test/resources/fixtures`, read as their grammar sees them. */
object Fixtures {
    fun text(file: String): String = Fixtures::class.java.getResource("/fixtures/$file")!!.readText()

    fun facts(file: String): FileFacts = FactExtractor.extract(Grammars.forPath(file)!!, text(file))
}

package dev.chieppa.arc.index.nav

import dev.chieppa.arc.syntax.facts.FactExtractor
import dev.chieppa.arc.syntax.grammar.Grammars
import kotlin.test.Test
import kotlin.test.assertEquals

class OutlineTest {
    @Test
    fun definitionsNestUnderTheirTypesAndLocalsAreLeftOut() {
        val text = "class TokenService(private val clock: Clock) {\n    val log = 1\n    fun refresh(token: String): TokenPair {\n        val pair = 2\n        return pair\n    }\n}\n\nclass TokenReuseException\n"
        val symbols = TreeSitterOutline.symbols(FactExtractor.extract(Grammars.kotlin, text).definitions)
        assertEquals(listOf("TokenService", "TokenReuseException"), symbols.map { it.name })
        val members = symbols.first().children
        assertEquals(listOf("clock", "log", "refresh"), members.map { it.name })
        assertEquals(listOf("token"), members.last().parameters)
        assertEquals("TokenPair", members.last().type)
        assertEquals("function", members.last().kind)
    }
}

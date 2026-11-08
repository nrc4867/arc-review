package dev.chieppa.arc.syntax.runtime

import dev.chieppa.arc.syntax.grammar.Grammars
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SyntaxQueriesTest {
    @Test
    fun everyQueryCompilesAgainstItsGrammar() {
        for (grammar in Grammars.all) {
            for (kind in QueryKind.entries) {
                if (SyntaxQueries.source(grammar, kind) == null) continue
                assertNotNull(SyntaxQueries.of(grammar, kind), "${grammar.id} ${kind.file}")
            }
            SyntaxQueries.errors(grammar)
        }
    }

    @Test
    fun everyGrammarHasHighlightsDefinitionsAndNames() {
        for (grammar in Grammars.all) {
            for (kind in listOf(QueryKind.Highlights, QueryKind.Definitions, QueryKind.Names)) {
                assertTrue(SyntaxQueries.source(grammar, kind) != null, "${grammar.id} has no ${kind.file}")
            }
        }
    }
}

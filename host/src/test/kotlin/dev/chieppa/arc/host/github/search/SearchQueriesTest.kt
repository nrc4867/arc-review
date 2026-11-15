package dev.chieppa.arc.host.github.search

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SearchQueriesTest {
    @Test
    fun aWellFormedQueryHasNoProblem() {
        assertNull(SearchQueries.problem("is:pr is:open label:\"needs review\" -label:wip (author:a OR author:b)"))
        assertNull(SearchQueries.problem("is:pr \"a: b\""))
    }

    @Test
    fun eachProblemIsNamed() {
        assertEquals(SearchProblem.UnclosedQuote, SearchQueries.problem("label:\"open")?.problem)
        assertEquals(SearchReading.Refused(SearchProblem.EmptyQualifier, "-label:"), SearchQueries.problem("is:pr -label:"))
        assertEquals(SearchProblem.UnbalancedParentheses, SearchQueries.problem("(author:a OR author:b")?.problem)
        assertEquals(SearchProblem.UnbalancedParentheses, SearchQueries.problem("author:a)")?.problem)
    }

    @Test
    fun wordsKeepQuotedRunsWhole() {
        assertEquals(listOf("label:\"needs review\"", "is:pr"), SearchQueries.words(" label:\"needs review\"  is:pr"))
    }
}

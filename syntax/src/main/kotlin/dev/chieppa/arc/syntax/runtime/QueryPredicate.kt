package dev.chieppa.arc.syntax.runtime

/**
 * A text predicate on a query pattern, such as `(#eq? @name "constructor")` or `(#match? @constant "^[A-Z]")`. Arc
 * applies the ones that filter matches; directives such as `#set!` and unknown predicates pass every match.
 */
internal sealed interface QueryPredicate {
    /** Whether a match passes, given the texts each capture index captured in it. */
    fun test(texts: (capture: Int) -> List<String>): Boolean

    /** `#eq?` and `#not-eq?`, and their `any-` forms, against a string or another capture. */
    class Equals(val capture: Int, val other: Int?, val value: String?, val negate: Boolean, val any: Boolean) : QueryPredicate {
        override fun test(texts: (capture: Int) -> List<String>): Boolean {
            val nodes = texts(capture)
            val otherText = other?.let { texts(it).firstOrNull() }
            val wanted = value ?: otherText ?: return true
            return passes(nodes, any, negate) { it == wanted }
        }
    }

    /** `#match?` and `#not-match?`, and their `any-` forms. */
    class Matches(val capture: Int, val regex: Regex, val negate: Boolean, val any: Boolean) : QueryPredicate {
        override fun test(texts: (capture: Int) -> List<String>): Boolean = passes(texts(capture), any, negate) { regex.containsMatchIn(it) }
    }

    /** `#any-of?` and `#not-any-of?`. */
    class AnyOf(val capture: Int, val values: Set<String>, val negate: Boolean) : QueryPredicate {
        override fun test(texts: (capture: Int) -> List<String>): Boolean = passes(texts(capture), any = false, negate) { it in values }
    }

    companion object {
        /** Whether [nodes] pass: every node (or one, for [any]) satisfies [check], or with [negate] fails it. */
        fun passes(nodes: List<String>, any: Boolean, negate: Boolean, check: (String) -> Boolean): Boolean {
            if (nodes.isEmpty()) return true
            val each = { text: String -> check(text) != negate }
            return if (any) nodes.any(each) else nodes.all(each)
        }
    }
}

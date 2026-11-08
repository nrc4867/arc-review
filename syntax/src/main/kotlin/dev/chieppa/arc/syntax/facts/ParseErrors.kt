package dev.chieppa.arc.syntax.facts

import dev.chieppa.arc.syntax.runtime.SyntaxQueries
import dev.chieppa.arc.syntax.runtime.SyntaxTree

/** How much of a tree failed to parse. */
object ParseErrors {
    /** How many characters lie under ERROR nodes, nested ones counted once. */
    fun chars(tree: SyntaxTree): Int {
        val records = SyntaxQueries.errors(tree.grammar).run(tree)
        var total = 0
        var coveredUntil = -1
        val spans = (0 until records.size).map { records.start(it) to records.end(it) }.sortedBy { it.first }
        for ((start, end) in spans) {
            val from = maxOf(start, coveredUntil)
            if (end > from) total += end - from
            coveredUntil = maxOf(coveredUntil, end)
        }
        return total
    }
}

package dev.chieppa.arc.demo

/** Where the sample threads sit in a file: spread over its changed lines, one context line above for the resolved one. */
data class SampleLines(val resolved: Int?, val open: Int?, val guest: Int?, val addressed: Int?, val draft: Int?) {
    companion object {
        private const val FEW = 3

        /** Distinct lines among [changed] (sorted), and the line above the first for the resolved thread. */
        fun spread(changed: List<Int>, lineCount: Int): SampleLines {
            val first = changed.first()
            val resolved = (first - 1).takeIf { it >= 1 && it <= lineCount && it !in changed }
            if (changed.size < FEW) return SampleLines(resolved, changed.first(), changed.getOrNull(1), null, null)
            val picks = listOf(0, changed.size / 4, changed.size / 2, changed.lastIndex).map { changed[it] }.distinct()
            return SampleLines(resolved, picks.getOrNull(0), picks.getOrNull(1), picks.getOrNull(2), picks.getOrNull(3))
        }
    }
}

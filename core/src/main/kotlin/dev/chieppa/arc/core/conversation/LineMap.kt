package dev.chieppa.arc.core.conversation

/**
 * Where each line of an old text is in a new one, for moving comments with their lines when a source moves. Lines the
 * two texts share at their start and end always map; between them, lines map along the longest run they have in
 * common, worked out only while the part between is small enough to compare whole.
 */
class LineMap private constructor(private val target: IntArray) {
    /** Where 1-based [line] of the old text is in the new one, or null when it is gone or changed. */
    fun map(line: Int): Int? = target.getOrNull(line - 1)?.takeIf { it > 0 }

    companion object {
        /** The most lines between the shared start and end that are compared line by line, on each side multiplied. */
        const val MAX_COMPARED = 4_000_000L

        fun of(old: List<String>, new: List<String>): LineMap {
            val target = IntArray(old.size)
            var prefix = 0
            while (prefix < old.size && prefix < new.size && old[prefix] == new[prefix]) {
                target[prefix] = prefix + 1
                prefix++
            }
            var suffix = 0
            while (suffix < old.size - prefix && suffix < new.size - prefix && old[old.size - 1 - suffix] == new[new.size - 1 - suffix]) {
                target[old.size - 1 - suffix] = new.size - suffix
                suffix++
            }
            val oldMiddle = old.subList(prefix, old.size - suffix)
            val newMiddle = new.subList(prefix, new.size - suffix)
            if (oldMiddle.isNotEmpty() && newMiddle.isNotEmpty() && oldMiddle.size.toLong() * newMiddle.size <= MAX_COMPARED) {
                common(oldMiddle, newMiddle).forEach { (o, n) -> target[prefix + o] = prefix + n + 1 }
            }
            return LineMap(target)
        }

        /** The pairs of indexes along the longest common run of [a] and [b]. */
        private fun common(a: List<String>, b: List<String>): List<Pair<Int, Int>> {
            val lengths = Array(a.size + 1) { IntArray(b.size + 1) }
            for (i in a.indices.reversed()) {
                for (j in b.indices.reversed()) {
                    lengths[i][j] = if (a[i] == b[j]) lengths[i + 1][j + 1] + 1 else maxOf(lengths[i + 1][j], lengths[i][j + 1])
                }
            }
            val pairs = mutableListOf<Pair<Int, Int>>()
            var i = 0
            var j = 0
            while (i < a.size && j < b.size) {
                when {
                    a[i] == b[j] -> pairs += (i++ to j++)
                    lengths[i + 1][j] >= lengths[i][j + 1] -> i++
                    else -> j++
                }
            }
            return pairs
        }
    }
}

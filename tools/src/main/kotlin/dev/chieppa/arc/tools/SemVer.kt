package dev.chieppa.arc.tools

/**
 * A semantic version, `major.minor.patch` with an optional `-label` of dot-separated identifiers, ordered as semver
 * orders them: a label comes before the plain version, numeric identifiers compare as numbers and before words.
 */
data class SemVer(val major: Int, val minor: Int, val patch: Int, val label: List<String> = emptyList()) : Comparable<SemVer> {
    /** Whether it carries a label, as `0.2.0-beta.1` does. */
    val prerelease: Boolean get() = label.isNotEmpty()

    override fun compareTo(other: SemVer): Int {
        compareValuesBy(this, other, SemVer::major, SemVer::minor, SemVer::patch).takeIf { it != 0 }?.let { return it }
        if (label.isEmpty() || other.label.isEmpty()) return other.label.size.coerceAtMost(1) - label.size.coerceAtMost(1)
        for (i in 0 until minOf(label.size, other.label.size)) {
            val order = identifier(label[i], other.label[i])
            if (order != 0) return order
        }
        return label.size - other.label.size
    }

    override fun toString(): String = "$major.$minor.$patch" + if (label.isEmpty()) "" else "-" + label.joinToString(".")

    companion object {
        private val PATTERN = Regex("""^v?(\d+)\.(\d+)\.(\d+)(?:-([0-9A-Za-z.-]+))?(?:\+.*)?$""")

        /** [text] as a version, with or without a leading `v`; null when it is not one. */
        fun parse(text: String): SemVer? {
            val match = PATTERN.matchEntire(text.trim()) ?: return null
            val (major, minor, patch, label) = match.destructured
            return SemVer(major.toInt(), minor.toInt(), patch.toInt(), label.takeIf { it.isNotEmpty() }?.split('.').orEmpty())
        }

        private fun identifier(a: String, b: String): Int {
            val x = a.toLongOrNull()
            val y = b.toLongOrNull()
            return when {
                x != null && y != null -> x.compareTo(y)
                x != null -> -1
                y != null -> 1
                else -> a.compareTo(b)
            }
        }
    }
}

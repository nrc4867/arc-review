package dev.chieppa.arc.ui.switcher

/** A sha typed into the switcher, or two of them as `from..to`: each at least [MIN_LENGTH] hex digits. */
data class CommitQuery(val from: String?, val to: String) {
    companion object {
        private const val MIN_LENGTH = 7
        private const val MAX_LENGTH = 40
        private const val RANGE = ".."
        private val SHA = Regex("[0-9a-fA-F]{$MIN_LENGTH,$MAX_LENGTH}")

        /** What [text] names, or null when it is not a sha or a range of two. */
        fun read(text: String): CommitQuery? {
            val trimmed = text.trim()
            val parts = trimmed.split(RANGE)
            return when {
                parts.size == 1 && SHA.matches(trimmed) -> CommitQuery(null, trimmed.lowercase())
                parts.size == 2 && parts.all { SHA.matches(it) } -> CommitQuery(parts[0].lowercase(), parts[1].lowercase())
                else -> null
            }
        }
    }
}

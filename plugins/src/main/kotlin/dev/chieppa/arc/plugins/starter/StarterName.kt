package dev.chieppa.arc.plugins.starter

/**
 * A new plugin's name as the person typed it, shown as its [title], and the [id] made from it: lowercase, runs of
 * anything but letters and digits as one dash. The id names its folder too.
 */
data class StarterName(val title: String, val id: String) {
    companion object {
        private val ALLOWED = Regex("""[\p{L}\p{N} ._-]+""")
        private val SEPARATORS = Regex("[^a-z0-9]+")
        private const val DASH = "-"
        private const val MAX_ID = 64

        /** The name [typed], or null when it makes no id or holds a character a name may not hold. */
        fun of(typed: String): StarterName? {
            val title = typed.trim()
            if (!ALLOWED.matches(title)) return null
            val id = title.lowercase().replace(SEPARATORS, DASH).trim('-').take(MAX_ID).trimEnd('-')
            return if (id.isEmpty()) null else StarterName(title, id)
        }
    }
}

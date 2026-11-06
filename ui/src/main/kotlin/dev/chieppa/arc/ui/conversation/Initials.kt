package dev.chieppa.arc.ui.conversation

/** The initials an avatar shows for a name: the first letters of its first two parts, `mfischer` giving `MF`. */
object Initials {
    private const val MAX = 2
    private val PARTS = Regex("[\\s._-]+")

    fun of(name: String): String {
        val parts = name.split(PARTS).filter { it.isNotEmpty() }
        val letters = if (parts.size >= MAX) parts.take(MAX).map { it.first() } else name.take(MAX).toList()
        return letters.joinToString("").uppercase()
    }
}

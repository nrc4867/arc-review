package dev.chieppa.arc.core.settings

/**
 * Finds settings by what a person types. Every word of the query must appear, ignoring case, in an entry's label,
 * detail or keywords, or in its card's title or its section's label; a hosted card is found whole by its title,
 * keywords and section label. The parts of the label the words match are given for marking.
 */
object SettingsSearch {
    private val SPACES = Regex("\\s+")

    /** The query's words, lowercased; none for a blank query. */
    fun words(query: String): List<String> = query.trim().lowercase().split(SPACES).filter { it.isNotEmpty() }

    /** The sections of [sections] with entries [query] finds, in their order; empty for a blank query. */
    fun find(sections: List<SettingsOwned>, query: String): List<SettingsFound> {
        val words = words(query)
        if (words.isEmpty()) return emptyList()
        return sections.mapNotNull { owned ->
            val matches = mutableMapOf<String, List<IntRange>>()
            val cards = owned.section.cards.mapNotNull { card ->
                if (card.hosted) {
                    val text = (listOf(card.title, owned.section.label) + card.keywords).map { it.lowercase() }
                    return@mapNotNull card.takeIf { words.all { word -> text.any { it.contains(word) } } }
                }
                val entries = card.entries.filter { entry ->
                    val text = (listOfNotNull(entry.label, entry.detail, card.title, owned.section.label) + entry.keywords).map { it.lowercase() }
                    words.all { word -> text.any { it.contains(word) } }
                }
                entries.forEach { matches[it.id] = ranges(it.label, words) }
                card.copy(entries = entries).takeIf { entries.isNotEmpty() }
            }
            SettingsFound(owned, cards, matches).takeIf { cards.isNotEmpty() }
        }
    }

    /** Where [words] appear in [label], ignoring case, in order and without overlaps. */
    fun ranges(label: String, words: List<String>): List<IntRange> {
        val lower = label.lowercase()
        val found = words.flatMap { word ->
            generateSequence(lower.indexOf(word).takeIf { it >= 0 }) { at -> lower.indexOf(word, at + word.length).takeIf { it >= 0 } }
                .map { it until it + word.length }
                .toList()
        }.sortedBy { it.first }
        return found.fold(emptyList<IntRange>()) { kept, range -> if (kept.isNotEmpty() && range.first <= kept.last().last) kept else kept + listOf(range) }
    }
}

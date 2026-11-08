package dev.chieppa.arc.core.conversation

import dev.chieppa.arc.core.source.Source

/** A source's notes as one Markdown file: its note on the whole source, then each note on a line, by path and line. */
object NotesExport {
    private const val HEADING = "# "
    private const val SECTION = "## "
    private const val ITEM = "- "
    private const val CODE = "`"

    /** The Markdown for [source]'s notes in [local], headed [title] and with its line notes under [onLines]. */
    fun markdown(local: LocalConversation, source: Source, title: String, onLines: String): String = buildString {
        val notes = local.of(source).filter { it.isNote }
        append(HEADING).append(title).append("\n\n")
        notes.firstOrNull { it.anchor == null }?.body?.trim()?.takeIf { it.isNotEmpty() }?.let { append(it).append("\n\n") }
        val anchored = notes.mapNotNull { note -> note.anchor?.let { it to note } }.sortedWith(compareBy({ it.first.path }, { it.first.line }))
        if (anchored.isEmpty()) return@buildString
        append(SECTION).append(onLines).append("\n\n")
        for ((anchor, note) in anchored) {
            append(ITEM).append(CODE).append(anchor.path).append(':').append(anchor.line).append(CODE).append(' ')
            append(note.body.trim().replace("\n", "\n  ")).append('\n')
        }
    }

    /** The file name for [source]'s notes: its ref with anything a file name cannot hold made a dash. */
    fun fileName(source: Source): String = source.ref.replace(Regex("[^A-Za-z0-9._-]+"), "-") + ".md"
}

package dev.chieppa.arc.syntax.docs

/**
 * Cuts comment lines into description and block tags. A block tag starts a line with `@name`, except inside a fenced
 * or `<pre>` block, and inside the body of [verbatimTag], where only a tag in [knownTags] starts one.
 */
internal class TagSplitter(private val verbatimTag: String? = null, private val knownTags: Set<String> = emptySet()) {
    fun split(lines: List<String>, firstLine: Int): SplitComment {
        val description = mutableListOf<String>()
        val tags = mutableListOf<RawTag>()
        var currentName: String? = null
        var currentBody = mutableListOf<String>()
        var currentLine = 0
        var fenced = false
        var preDepth = 0
        fun flush() {
            currentName?.let { tags += RawTag(it, currentBody, currentLine) }
        }
        lines.forEachIndexed { index, line ->
            val trimmed = line.trim()
            val match = if (fenced || preDepth > 0) null else TAG.matchEntire(trimmed)
            val name = match?.groupValues?.get(1)
            val inVerbatim = verbatimTag != null && currentName == verbatimTag
            if (match != null && name != null && (!inVerbatim || name in knownTags)) {
                flush()
                currentName = name
                currentBody = mutableListOf(match.groupValues[2].trimStart())
                currentLine = firstLine + index
            } else if (currentName != null) {
                currentBody.add(line)
            } else {
                description.add(line)
            }
            if (trimmed.startsWith(DocSyntax.FENCE)) fenced = !fenced
            preDepth = (preDepth + count(trimmed, DocSyntax.PRE_OPEN) - count(trimmed, DocSyntax.PRE_CLOSE))
                .coerceAtLeast(0)
        }
        flush()
        return SplitComment(description, tags)
    }

    private fun count(text: String, needle: String): Int =
        text.windowed(needle.length).count { it.equals(needle, ignoreCase = true) }

    private companion object {
        val TAG = Regex("""@([A-Za-z][A-Za-z0-9]*)(?=$|[\s\[])(.*)""")
    }
}

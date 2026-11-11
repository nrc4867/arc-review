package dev.chieppa.arc.syntax.docs

/** A block tag as written: its [name], its [lines] (the first is what follows the name) and the file [line] it starts on. */
internal data class RawTag(val name: String, val lines: List<String>, val line: Int) {
    val text: String get() = CommentText.join(lines)
}

/** A comment cut into the lines before the first block tag and its tags. */
internal data class SplitComment(val description: List<String>, val tags: List<RawTag>)

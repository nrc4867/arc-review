package dev.chieppa.arc.syntax.highlight

/** Cuts whole-text spans into each line's spans, with offsets from the start of their line. */
object LineSpans {
    fun split(text: String, spans: List<HighlightSpan>): List<List<HighlightSpan>> {
        val starts = ArrayList<Int>().apply { add(0) }
        text.forEachIndexed { i, c -> if (c == '\n') starts += i + 1 }
        val lines = List(starts.size) { ArrayList<HighlightSpan>() }
        var line = 0
        for (span in spans) {
            var start = span.start
            while (start < span.end) {
                while (line + 1 < starts.size && starts[line + 1] <= start) line++
                val lineEnd = if (line + 1 < starts.size) starts[line + 1] - 1 else text.length
                val end = minOf(span.end, lineEnd)
                if (end > start) lines[line] += HighlightSpan(start - starts[line], end - starts[line], span.style)
                start = maxOf(end, lineEnd + 1)
            }
        }
        return lines
    }
}

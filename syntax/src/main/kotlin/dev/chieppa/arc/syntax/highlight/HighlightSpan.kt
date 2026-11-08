package dev.chieppa.arc.syntax.highlight

/** Characters [start] until [end] draw as [style]. Spans of one result are ordered and do not overlap. */
data class HighlightSpan(val start: Int, val end: Int, val style: TokenStyle)

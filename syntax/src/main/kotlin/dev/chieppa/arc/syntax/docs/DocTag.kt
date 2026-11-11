package dev.chieppa.arc.syntax.docs

/**
 * One block tag of a doc comment: its [kind], the [name] it is about (a parameter, an exception, or the tag's own name
 * for [DocTagKind.Other]), a [type] written in braces (JSDoc), its [text] as Markdown, and the [line] in the file it
 * starts on, from 1.
 */
data class DocTag(val kind: DocTagKind, val name: String?, val type: String?, val text: String, val line: Int)

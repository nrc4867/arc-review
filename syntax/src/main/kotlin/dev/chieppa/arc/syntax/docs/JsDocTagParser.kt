package dev.chieppa.arc.syntax.docs

/** Reads one JSDoc or TSDoc block tag. */
internal object JsDocTagParser {
    fun parse(tag: RawTag, language: String): DocTag {
        val text = tag.text
        return when (tag.name) {
            DocTagNames.PARAM, DocTagNames.ARG, DocTagNames.ARGUMENT -> param(text, tag.line)
            DocTagNames.RETURN, DocTagNames.RETURNS -> {
                val (type, rest) = TextSplit.braced(text)
                DocTag(DocTagKind.Returns, null, type, description(rest), tag.line)
            }
            DocTagNames.THROWS, DocTagNames.EXCEPTION -> {
                val (type, rest) = TextSplit.braced(text)
                DocTag(DocTagKind.Throws, type, type, description(rest), tag.line)
            }
            DocTagNames.TEMPLATE, DocTagNames.TYPE_PARAM -> {
                val (type, rest) = TextSplit.braced(text)
                val (name, desc) = TextSplit.firstWord(rest)
                DocTag(DocTagKind.TypeParam, name.trimEnd(','), type, description(desc), tag.line)
            }
            DocTagNames.EXAMPLE -> DocTag(DocTagKind.Example, null, null, JsDocExample.toMarkdown(tag.lines, language), tag.line)
            DocTagNames.SEE -> plain(DocTagKind.See, null, text, tag.line)
            DocTagNames.SINCE -> plain(DocTagKind.Since, null, text, tag.line)
            DocTagNames.AUTHOR -> plain(DocTagKind.Author, null, text, tag.line)
            else -> plain(DocTagKind.Other, tag.name, text, tag.line)
        }
    }

    private fun plain(kind: DocTagKind, name: String?, text: String, line: Int) =
        DocTag(kind, name, null, JsDocLinks.rewrite(text), line)

    private fun description(text: String): String = JsDocLinks.rewrite(text.removeDash())

    private fun String.removeDash(): String = if (this == "-") "" else removePrefix("- ").trimStart()

    private fun param(text: String, line: Int): DocTag {
        val (type, afterType) = TextSplit.braced(text)
        var name: String
        var default: String? = null
        var rest: String
        if (afterType.startsWith("[")) {
            val end = TextSplit.matching(afterType, 0, '[', ']') ?: afterType.length - 1
            val inner = afterType.substring(1, end)
            name = inner.substringBefore('=').trim()
            if (inner.contains('=')) default = inner.substringAfter('=').trim()
            rest = afterType.substring(end + 1).trim()
        } else {
            val (word, remainder) = TextSplit.firstWord(afterType)
            name = word
            rest = remainder
        }
        var body = description(rest)
        if (default != null) {
            val note = "(default ${MarkdownCode.span(default)})"
            body = if (body.isEmpty()) note else "$body $note"
        }
        return DocTag(DocTagKind.Param, name, type, body, line)
    }
}

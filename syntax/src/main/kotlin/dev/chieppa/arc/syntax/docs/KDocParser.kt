package dev.chieppa.arc.syntax.docs

/** Reads a KDoc comment. */
object KDocParser {
    /** The comment [raw] (delimiters included) that starts on file line [firstLine]. */
    fun parse(raw: String, firstLine: Int): DocComment {
        val split = TagSplitter().split(CommentText.blockLines(raw), firstLine)
        var deprecated: String? = null
        val tags = mutableListOf<DocTag>()
        for (tag in split.tags) {
            if (tag.name == DocTagNames.DEPRECATED) deprecated = KDocLinks.rewrite(tag.text) else tags += convert(tag)
        }
        return DocComment(
            DocFormat.KDoc,
            KDocLinks.rewrite(CommentText.join(split.description)),
            deprecated,
            tags,
            firstLine,
            firstLine + raw.count { it == '\n' },
        )
    }

    private fun convert(tag: RawTag): DocTag {
        val text = tag.text
        fun make(kind: DocTagKind, name: String?, body: String) = DocTag(kind, name, null, KDocLinks.rewrite(body), tag.line)
        return when (tag.name) {
            DocTagNames.PARAM -> named(text).let { (name, rest) -> make(DocTagKind.Param, name, rest) }
            DocTagNames.PROPERTY -> named(text).let { (name, rest) -> make(DocTagKind.Property, name, rest) }
            DocTagNames.RETURN, DocTagNames.RETURNS -> make(DocTagKind.Returns, null, text)
            DocTagNames.THROWS, DocTagNames.EXCEPTION -> named(text).let { (name, rest) -> make(DocTagKind.Throws, name, rest) }
            DocTagNames.SEE -> reference(DocTagKind.See, text, tag.line)
            DocTagNames.SAMPLE -> reference(DocTagKind.Example, text, tag.line)
            DocTagNames.SINCE -> make(DocTagKind.Since, null, text)
            DocTagNames.AUTHOR -> make(DocTagKind.Author, null, text)
            else -> make(DocTagKind.Other, tag.name, text)
        }
    }

    private fun reference(kind: DocTagKind, text: String, line: Int): DocTag {
        val (target, rest) = TextSplit.firstWord(text)
        val link = SymbolLinks.render(target, target)
        return DocTag(kind, null, null, if (rest.isEmpty()) link else "$link ${KDocLinks.rewrite(rest)}", line)
    }

    private fun named(text: String): Pair<String, String> {
        if (!text.startsWith("[")) return TextSplit.firstWord(text)
        val end = text.indexOf(']')
        if (end < 0) return TextSplit.firstWord(text)
        return text.substring(1, end).trim() to text.substring(end + 1).trim()
    }
}

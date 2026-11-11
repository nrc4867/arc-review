package dev.chieppa.arc.syntax.docs

/** Reads a JavaDoc comment. */
object JavaDocParser {
    /** The comment [raw] (delimiters included) that starts on file line [firstLine]. */
    fun parse(raw: String, firstLine: Int): DocComment {
        val split = TagSplitter().split(CommentText.blockLines(raw), firstLine)
        var deprecated: String? = null
        val tags = mutableListOf<DocTag>()
        for (tag in split.tags) {
            if (tag.name == DocTagNames.DEPRECATED) deprecated = JavaDocHtml.toMarkdown(tag.text) else tags += convert(tag)
        }
        return DocComment(
            DocFormat.JavaDoc,
            JavaDocHtml.toMarkdown(CommentText.join(split.description)),
            deprecated,
            tags,
            firstLine,
            firstLine + raw.count { it == '\n' },
        )
    }

    private fun convert(tag: RawTag): DocTag {
        val text = tag.text
        fun make(kind: DocTagKind, name: String?, body: String) = DocTag(kind, name, null, JavaDocHtml.toMarkdown(body), tag.line)
        return when (tag.name) {
            DocTagNames.PARAM -> {
                val (word, rest) = TextSplit.firstWord(text)
                if (word.startsWith("<")) make(DocTagKind.TypeParam, word.removeSurrounding("<", ">"), rest) else make(DocTagKind.Param, word, rest)
            }
            DocTagNames.RETURN -> make(DocTagKind.Returns, null, text)
            DocTagNames.THROWS, DocTagNames.EXCEPTION -> {
                val (word, rest) = TextSplit.firstWord(text)
                make(DocTagKind.Throws, word, rest)
            }
            DocTagNames.SEE -> DocTag(DocTagKind.See, null, null, see(text), tag.line)
            DocTagNames.SINCE -> make(DocTagKind.Since, null, text)
            DocTagNames.AUTHOR -> make(DocTagKind.Author, null, text)
            else -> make(DocTagKind.Other, tag.name, text)
        }
    }

    private fun see(text: String): String = when {
        text.startsWith("\"") || SymbolLinks.isUrl(text) -> text
        text.startsWith("<") -> JavaDocHtml.toMarkdown(text)
        else -> SymbolLinks.splitBody(text).let { (target, label) -> JavaDocInline.reference(target, label) }
    }
}

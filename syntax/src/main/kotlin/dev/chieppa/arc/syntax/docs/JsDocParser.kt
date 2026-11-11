package dev.chieppa.arc.syntax.docs

/** Reads a JSDoc or TSDoc comment. */
object JsDocParser {
    private const val TYPESCRIPT_FENCE = "ts"
    private const val JAVASCRIPT_FENCE = "js"

    /** The comment [raw] (delimiters included) that starts on file line [firstLine]; examples are fenced as TypeScript when [typescript]. */
    fun parse(raw: String, firstLine: Int, typescript: Boolean): DocComment {
        val splitter = TagSplitter(DocTagNames.EXAMPLE, DocTagNames.javaScriptKnown)
        val split = splitter.split(CommentText.blockLines(raw), firstLine)
        val language = if (typescript) TYPESCRIPT_FENCE else JAVASCRIPT_FENCE
        var description = JsDocLinks.rewrite(CommentText.join(split.description))
        var deprecated: String? = null
        val tags = mutableListOf<DocTag>()
        for (tag in split.tags) {
            when (tag.name) {
                DocTagNames.REMARKS -> description = listOf(description, JsDocLinks.rewrite(tag.text)).filter { it.isNotEmpty() }.joinToString("\n\n")
                DocTagNames.DEPRECATED -> deprecated = JsDocLinks.rewrite(tag.text)
                else -> tags += JsDocTagParser.parse(tag, language)
            }
        }
        return DocComment(DocFormat.JsDoc, description, deprecated, tags, firstLine, firstLine + raw.count { it == '\n' })
    }
}

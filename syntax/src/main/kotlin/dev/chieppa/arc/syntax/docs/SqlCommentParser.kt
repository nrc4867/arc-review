package dev.chieppa.arc.syntax.docs

/** Reads the comments above a T-SQL definition: a run of line comments or one block comment. */
object SqlCommentParser {
    /** The comment [raw] (delimiters included) that starts on file line [firstLine]. */
    fun parse(raw: String, firstLine: Int): DocComment {
        val lines = if (raw.startsWith(DocSyntax.BLOCK_OPEN)) CommentText.blockLines(raw) else lineComments(raw)
        val description = CommentText.join(lines.map { SqlMarkdownEscape.line(it) })
        return DocComment(DocFormat.SqlComment, description, null, emptyList(), firstLine, firstLine + raw.count { it == '\n' })
    }

    private fun lineComments(raw: String): List<String> =
        raw.split('\n').map { it.trim().removePrefix(DocSyntax.SQL_LINE) }
}

package dev.chieppa.arc.syntax.docs

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SqlCommentParserTest {
    @Test
    fun lineCommentsKeepBreaksAndEscapeMarkdown() {
        val doc = SqlCommentParser.parse("-- Orders by *customer*\n--  uses dbo_Orders\n--\n-- 1. first\n-- # not a heading", 4)
        assertEquals(
            "Orders by \\*customer\\*\nuses dbo\\_Orders\n\n1\\. first\n\\# not a heading",
            doc.description,
        )
        assertEquals(DocFormat.SqlComment, doc.format)
        assertEquals(4, doc.firstLine)
        assertEquals(8, doc.lastLine)
        assertEquals(0, doc.tags.size)
        assertNull(doc.deprecated)
    }

    @Test
    fun blockCommentIsStripped() {
        val doc = SqlCommentParser.parse("/*\n * Loads <rows>\n * quickly\n */", 1)
        assertEquals("Loads \\<rows\\>\nquickly", doc.description)
        assertEquals(4, doc.lastLine)
    }
}

package dev.chieppa.arc.syntax.highlight

import dev.chieppa.arc.syntax.facts.Fixtures
import dev.chieppa.arc.syntax.grammar.Grammars
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HighlighterTest {
    private fun styled(text: String, spans: List<HighlightSpan>) = spans.associate { text.substring(it.start, it.end) to it.style }

    @Test
    fun keywordsTypesStringsAndCommentsInKotlin() {
        val text = "// note\nclass A(val b: String) { fun c() = \"d\" }\n"
        val spans = assertNotNull(Highlighter.highlight(Grammars.kotlin, text))
        val styles = styled(text, spans)
        assertEquals(TokenStyle.Comment, styles["// note"])
        assertEquals(TokenStyle.Keyword, styles["class"])
        assertEquals(TokenStyle.Type, styles["String"])
        assertEquals(TokenStyle.String, styles["\"d\""])
        assertTrue(spans.zipWithNext().all { (a, b) -> a.end <= b.start })
    }

    @Test
    fun aSqlFileTooBrokenToReadIsLeftToTheViewer() {
        assertNull(Highlighter.highlight(Grammars.mssql, "))) (((( ;;; ))) (((( select from where ))) ((("))
        assertNotNull(Highlighter.highlight(Grammars.mssql, Fixtures.text("Orders.sql")))
    }

    @Test
    fun spansSplitIntoLinesWithLineOffsets() {
        val text = "ab\ncd\nef"
        val lines = LineSpans.split(text, listOf(HighlightSpan(1, 4, TokenStyle.String), HighlightSpan(6, 8, TokenStyle.Keyword)))
        assertEquals(listOf(HighlightSpan(1, 2, TokenStyle.String)), lines[0])
        assertEquals(listOf(HighlightSpan(0, 1, TokenStyle.String)), lines[1])
        assertEquals(listOf(HighlightSpan(0, 2, TokenStyle.Keyword)), lines[2])
    }
}

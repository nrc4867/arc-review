package dev.chieppa.arc.syntax.docs

import dev.chieppa.arc.syntax.grammar.Grammars
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class DocCommentsTest {
    private fun at(text: String, marker: String): Int = text.indexOf(marker).also { check(it >= 0) }

    @Test
    fun kotlinFunctionWithAnnotationBetween() {
        val text = "package a\n\n/**\n * Adds [x].\n * @param x the x\n */\n@Deprecated(\"no\")\nfun add(x: Int) = x\n"
        val doc = assertNotNull(DocComments.before(Grammars.kotlin, text, at(text, "@Deprecated")))
        assertEquals(DocFormat.KDoc, doc.format)
        assertEquals("Adds [x](symbol:x).", doc.description)
        assertEquals(3, doc.firstLine)
        assertEquals(6, doc.lastLine)
        assertEquals(5, doc.tags.single().line)
    }

    @Test
    fun javaDocWithBlankLineBetween() {
        val text = "class A {\n    /** Hello {@code x}. */\n\n    public void run() {}\n}"
        val doc = assertNotNull(DocComments.before(Grammars.java, text, at(text, "public")))
        assertEquals(DocFormat.JavaDoc, doc.format)
        assertEquals("Hello `x`.", doc.description)
        assertEquals(2, doc.firstLine)
    }

    @Test
    fun plainBlockAndLineCommentsAreNotDocs() {
        val block = "/* not doc */\nclass A {}"
        assertNull(DocComments.before(Grammars.java, block, at(block, "class")))
        val line = "// not doc\nclass A {}"
        assertNull(DocComments.before(Grammars.java, line, at(line, "class")))
        val empty = "/**/\nclass A {}"
        assertNull(DocComments.before(Grammars.java, empty, at(empty, "class")))
        assertNull(DocComments.before(Grammars.java, "class A {}", 0))
    }

    @Test
    fun docNotDirectlyBeforeIsIgnored() {
        val text = "/** doc */\nint other;\nclass A {}"
        assertNull(DocComments.before(Grammars.java, text, at(text, "class")))
    }

    @Test
    fun javaScriptSkipsKeywords() {
        val text = "/** Make it. */\nexport const make = () => 1;\n"
        val doc = assertNotNull(DocComments.before(Grammars.typescript, text, at(text, "make")))
        assertEquals(DocFormat.JsDoc, doc.format)
        assertEquals("Make it.", doc.description)
        val async = "/**\n * @example\n * go();\n */\nexport default async function go() {}"
        val example = assertNotNull(DocComments.before(Grammars.javascript, async, at(async, "go() {}"))).tags.single()
        assertEquals("```js\ngo();\n```", example.text)
        val tsx = "/** @example\n * a\n */\nexport function F() {}"
        val tsxDoc = assertNotNull(DocComments.before(Grammars.tsx, tsx, at(tsx, "F()")))
        assertEquals("```ts\na\n```", tsxDoc.tags.single().text)
    }

    @Test
    fun javaScriptKeywordInsideWordIsNotSkipped() {
        val text = "/** doc */\nmyexport x"
        assertNull(DocComments.before(Grammars.javascript, text, at(text, "x")))
    }

    @Test
    fun sqlLineRunDirectlyAbove() {
        val text = "SELECT 1;\n-- Gets orders\n-- by day\nCREATE PROCEDURE p AS SELECT 1;"
        val doc = assertNotNull(DocComments.before(Grammars.mssql, text, at(text, "CREATE")))
        assertEquals(DocFormat.SqlComment, doc.format)
        assertEquals("Gets orders\nby day", doc.description)
        assertEquals(2, doc.firstLine)
        assertEquals(3, doc.lastLine)
    }

    @Test
    fun sqlRunSeparatedByBlankLineIsIgnored() {
        val text = "-- Gets orders\n\nCREATE PROCEDURE p AS SELECT 1;"
        assertNull(DocComments.before(Grammars.mssql, text, at(text, "CREATE")))
        val trailing = "SELECT 1 -- trailing\nCREATE PROCEDURE p AS SELECT 1;"
        assertNull(DocComments.before(Grammars.mssql, trailing, at(trailing, "CREATE")))
    }

    @Test
    fun sqlBlockCommentBefore() {
        val text = "/* Gets orders */\n\nCREATE VIEW v AS SELECT 1;"
        assertEquals("Gets orders", DocComments.before(Grammars.mssql, text, at(text, "CREATE"))?.description)
    }
}

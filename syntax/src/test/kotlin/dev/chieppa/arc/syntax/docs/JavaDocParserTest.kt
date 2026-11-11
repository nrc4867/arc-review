package dev.chieppa.arc.syntax.docs

import kotlin.test.Test
import kotlin.test.assertEquals

class JavaDocParserTest {
    private fun parse(body: String) = JavaDocParser.parse(body.trimIndent(), 10)

    @Test
    fun htmlBecomesMarkdown() {
        val doc = parse(
            """
            /**
             * Reads <b>bold</b> and <i>soft</i> text, <code>x</code> and <tt>y</tt>.<p>Second &lt;paragraph&gt; &amp; &quot;more&quot; &#64;x&nbsp;y.
             * Line<br>break and <a href="https://e.org">a link</a>.
             * <ul><li>one</li><li>two</li></ul>
             * <ol><li>first</li></ol>
             * <span>kept</span>
             */
            """,
        )
        assertEquals(
            "Reads **bold** and *soft* text, `x` and `y`.\n\nSecond <paragraph> & \"more\" @x y.\n" +
                "Line  \nbreak and [a link](https://e.org).\n\n- one\n- two\n\n1. first\n\nkept",
            doc.description,
        )
    }

    @Test
    fun preWithCodeKeepsBracesAndIndentation() {
        val doc = parse(
            """
            /**
             * Example:
             * <pre>{@code
             * if (a) {
             *     run(Map<String, Integer>);
             * }
             * }</pre>
             */
            """,
        )
        assertEquals("Example:\n\n```java\nif (a) {\n    run(Map<String, Integer>);\n}\n```", doc.description)
    }

    @Test
    fun plainPreDecodesEntities() {
        val doc = parse("/**\n * <pre>\n * a &lt; b\n * </pre>\n */")
        assertEquals("```java\na < b\n```", doc.description)
    }

    @Test
    fun inlineTags() {
        val doc = parse(
            "/** {@code Map<String, {x}>} {@literal <b>} {@value MAX} {@link A#b(int) label} {@link A#b(int)} " +
                "{@linkplain Foo} {@inheritDoc}done <code>{@code y}</code> */",
        )
        assertEquals(
            "`Map<String, {x}>` <b> `MAX` [label](symbol:A#b) [`A.b`](symbol:A#b) [Foo](symbol:Foo) done `y`",
            doc.description,
        )
    }

    @Test
    fun blockTagsAndLineNumbers() {
        val doc = parse(
            """
            /**
             * Does it.
             * @param <T> the type
             * @param x the {@code x}
             *          value
             * @return the result
             * @throws IOException when bad
             * @exception IllegalStateException if wrong
             * @see java.util.List#add(Object) adding
             * @see "The Book"
             * @see https://example.org
             * @since 1.2
             * @author Ann
             * @deprecated use {@link Other}
             * @custom thing
             */
            """,
        )
        assertEquals("Does it.", doc.description)
        assertEquals("use [`Other`](symbol:Other)", doc.deprecated)
        assertEquals(10, doc.firstLine)
        assertEquals(25, doc.lastLine)
        val tags = doc.tags
        assertEquals(DocTag(DocTagKind.TypeParam, "T", null, "the type", 12), tags[0])
        assertEquals(DocTag(DocTagKind.Param, "x", null, "the `x`\n         value", 13), tags[1])
        assertEquals(DocTag(DocTagKind.Returns, null, null, "the result", 15), tags[2])
        assertEquals(DocTag(DocTagKind.Throws, "IOException", null, "when bad", 16), tags[3])
        assertEquals(DocTag(DocTagKind.Throws, "IllegalStateException", null, "if wrong", 17), tags[4])
        assertEquals("[adding](symbol:java.util.List#add)", tags[5].text)
        assertEquals("\"The Book\"", tags[6].text)
        assertEquals("https://example.org", tags[7].text)
        assertEquals(DocTag(DocTagKind.Since, null, null, "1.2", 21), tags[8])
        assertEquals(DocTag(DocTagKind.Author, null, null, "Ann", 22), tags[9])
        assertEquals(DocTag(DocTagKind.Other, "custom", null, "thing", 24), tags[10])
        assertEquals(11, tags.size)
    }

    @Test
    fun tagInsidePreIsNotATag() {
        val doc = parse("/**\n * Use:\n * <pre>\n * @Override\n * </pre>\n * @return x\n */")
        assertEquals(1, doc.tags.size)
        assertEquals("Use:\n\n```java\n@Override\n```", doc.description)
    }
}

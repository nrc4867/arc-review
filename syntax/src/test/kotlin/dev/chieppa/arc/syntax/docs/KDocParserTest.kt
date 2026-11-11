package dev.chieppa.arc.syntax.docs

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class KDocParserTest {
    private fun parse(body: String) = KDocParser.parse(body.trimIndent(), 5)

    @Test
    fun linksBecomeSymbolLinksOutsideCode() {
        val doc = parse(
            """
            /**
             * See [Foo] and [the label][Bar.baz], a real [link](https://e.org), `[Code]` and an array[0].
             * ```
             * [Fenced] stays
             * ```
             */
            """,
        )
        assertEquals(
            "See [Foo](symbol:Foo) and [the label](symbol:Bar.baz), a real [link](https://e.org), `[Code]` and an array[0].\n" +
                "```\n[Fenced] stays\n```",
            doc.description,
        )
    }

    @Test
    fun tags() {
        val doc = parse(
            """
            /**
             * Summary.
             * @constructor Makes it
             * @param a the [Foo]
             * @param[b] second
             * @property p a property
             * @return the value
             * @throws IllegalStateException when [Foo] is bad
             * @exception Error always
             * @see Foo.bar
             * @sample samples.Demo.run
             * @since 2.0
             * @author Bo
             * @receiver the receiver
             * @deprecated gone
             */
            """,
        )
        assertEquals("Summary.", doc.description)
        assertEquals("gone", doc.deprecated)
        assertEquals(
            listOf(
                DocTag(DocTagKind.Other, "constructor", null, "Makes it", 7),
                DocTag(DocTagKind.Param, "a", null, "the [Foo](symbol:Foo)", 8),
                DocTag(DocTagKind.Param, "b", null, "second", 9),
                DocTag(DocTagKind.Property, "p", null, "a property", 10),
                DocTag(DocTagKind.Returns, null, null, "the value", 11),
                DocTag(DocTagKind.Throws, "IllegalStateException", null, "when [Foo](symbol:Foo) is bad", 12),
                DocTag(DocTagKind.Throws, "Error", null, "always", 13),
                DocTag(DocTagKind.See, null, null, "[Foo.bar](symbol:Foo.bar)", 14),
                DocTag(DocTagKind.Example, null, null, "[samples.Demo.run](symbol:samples.Demo.run)", 15),
                DocTag(DocTagKind.Since, null, null, "2.0", 16),
                DocTag(DocTagKind.Author, null, null, "Bo", 17),
                DocTag(DocTagKind.Other, "receiver", null, "the receiver", 18),
            ),
            doc.tags,
        )
        assertEquals(5, doc.firstLine)
        assertEquals(20, doc.lastLine)
    }

    @Test
    fun noDeprecatedTagLeavesItNull() {
        assertNull(parse("/** Just text. */").deprecated)
    }

    @Test
    fun tagInsideFenceIsKept() {
        val doc = parse("/**\n * Use:\n * ```kotlin\n * @Suppress(\"x\")\n * ```\n */")
        assertEquals(0, doc.tags.size)
    }
}

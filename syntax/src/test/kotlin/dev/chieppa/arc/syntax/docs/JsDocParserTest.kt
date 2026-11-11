package dev.chieppa.arc.syntax.docs

import kotlin.test.Test
import kotlin.test.assertEquals

class JsDocParserTest {
    private fun parse(body: String, typescript: Boolean = false) = JsDocParser.parse(body.trimIndent(), 3, typescript)

    @Test
    fun paramForms() {
        val doc = parse(
            """
            /**
             * Does things.
             * @param {string} name the name
             * @param {number} [count=3] how many
             * @param {Object} [opts]
             * @param {boolean} opts.silent quiet
             * @param id - the id
             * @arg a first
             * @argument b second
             */
            """,
        )
        assertEquals(
            listOf(
                DocTag(DocTagKind.Param, "name", "string", "the name", 5),
                DocTag(DocTagKind.Param, "count", "number", "how many (default `3`)", 6),
                DocTag(DocTagKind.Param, "opts", "Object", "", 7),
                DocTag(DocTagKind.Param, "opts.silent", "boolean", "quiet", 8),
                DocTag(DocTagKind.Param, "id", null, "the id", 9),
                DocTag(DocTagKind.Param, "a", null, "first", 10),
                DocTag(DocTagKind.Param, "b", null, "second", 11),
            ),
            doc.tags,
        )
    }

    @Test
    fun returnsThrowsTemplates() {
        val doc = parse(
            """
            /**
             * @returns {Promise<void>} when done
             * @return nothing
             * @throws {TypeError} on bad input
             * @template T the item
             * @typeParam U - other
             * @default 5
             * @defaultValue six
             * @since 1.0
             * @author Cy
             * @see {@link Foo}
             */
            """,
        )
        assertEquals(
            listOf(
                DocTag(DocTagKind.Returns, null, "Promise<void>", "when done", 4),
                DocTag(DocTagKind.Returns, null, null, "nothing", 5),
                DocTag(DocTagKind.Throws, "TypeError", "TypeError", "on bad input", 6),
                DocTag(DocTagKind.TypeParam, "T", null, "the item", 7),
                DocTag(DocTagKind.TypeParam, "U", null, "other", 8),
                DocTag(DocTagKind.Other, "default", null, "5", 9),
                DocTag(DocTagKind.Other, "defaultValue", null, "six", 10),
                DocTag(DocTagKind.Since, null, null, "1.0", 11),
                DocTag(DocTagKind.Author, null, null, "Cy", 12),
                DocTag(DocTagKind.See, null, null, "[`Foo`](symbol:Foo)", 13),
            ),
            doc.tags,
        )
    }

    @Test
    fun exampleIsFencedPerLanguage() {
        val raw = "/**\n * @example\n * const a = 1;\n * @Decorator()\n * run(a);\n * @returns x\n */"
        val js = parse(raw).tags
        assertEquals("```js\nconst a = 1;\n@Decorator()\nrun(a);\n```", js[0].text)
        assertEquals(DocTagKind.Returns, js[1].kind)
        assertEquals("```ts\nconst a = 1;\n@Decorator()\nrun(a);\n```", parse(raw, typescript = true).tags[0].text)
    }

    @Test
    fun exampleCaptionAndExistingFence() {
        val captioned = parse("/**\n * @example <caption>Basic</caption>\n * go();\n */").tags.single()
        assertEquals("Basic\n\n```js\ngo();\n```", captioned.text)
        val fenced = parse("/**\n * @example\n * ```ts\n * go();\n * ```\n */").tags.single()
        assertEquals("```ts\ngo();\n```", fenced.text)
    }

    @Test
    fun remarksDeprecatedAndLinks() {
        val doc = parse(
            """
            /**
             * Uses {@link Foo}, {@link Foo|the foo}, {@link Foo the other}, {@linkcode Bar}, {@linkplain Baz},
             * [text]{@link Qux} and {@link https://e.org docs}; `{@link Kept}`.
             * @remarks More detail.
             * @deprecated Use {@link Next}.
             */
            """,
        )
        assertEquals(
            "Uses [`Foo`](symbol:Foo), [the foo](symbol:Foo), [the other](symbol:Foo), [`Bar`](symbol:Bar), " +
                "[Baz](symbol:Baz),\n[text](symbol:Qux) and [docs](https://e.org); `{@link Kept}`.\n\nMore detail.",
            doc.description,
        )
        assertEquals("Use [`Next`](symbol:Next).", doc.deprecated)
        assertEquals(0, doc.tags.size)
        assertEquals(DocFormat.JsDoc, doc.format)
    }
}

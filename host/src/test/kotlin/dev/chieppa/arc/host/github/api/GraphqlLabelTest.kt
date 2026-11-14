package dev.chieppa.arc.host.github.api

import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GraphqlLabelTest {
    private fun body(query: String) = buildJsonObject { put("query", query) }.toString()

    @Test
    fun aQueryIsNamedByItsTopFields() {
        val query = """
            query(${'$'}q: String!) {
              mine: search(query: ${'$'}q, type: ISSUE, first: 50) { nodes { ... on PullRequest { number title } } }
              repository(owner: "a", name: "b { not a field }") { id }
              rateLimit { remaining }
            }
        """.trimIndent()
        assertEquals("search, repository, rateLimit", GraphqlLabel.of(body(query)))
    }

    @Test
    fun aMutationSaysSo() {
        assertEquals("mutation addReaction", GraphqlLabel.of(body("mutation { addReaction(input: {subjectId: \"x\", content: HEART}) { reaction { content } } }")))
    }

    @Test
    fun aBodyWithoutAQueryHasNoLabel() {
        assertNull(GraphqlLabel.of("""{"title":"x"}"""))
        assertNull(GraphqlLabel.of(null))
        assertNull(GraphqlLabel.of("not json"))
    }
}

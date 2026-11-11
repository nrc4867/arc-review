package dev.chieppa.arc.codeintel.model

import dev.chieppa.arc.codeintel.Answers
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** The contracts are what a plugin reads and writes; they must survive JSON both ways. */
class ContractJsonTest {
    private val json = Json { encodeDefaults = false }

    @Test
    fun answersRoundTripThroughJson() {
        val usages = UsagesAnswer("TokenPair", listOf(UsageGroup("api#412", "9c1e2fa", "kotlin", Answers.byName, listOf(Answers.target("a/T.kt", 47)))))
        assertEquals(usages, json.decodeFromString<UsagesAnswer>(json.encodeToString(usages)))
        val decoration = CodeDecoration(CodeRange.onLine(3, 0, 5), DecorationStyle.Warning, Answers.exact, "**deprecated**")
        assertEquals(decoration, json.decodeFromString<CodeDecoration>(json.encodeToString(decoration)))
        val outline = OutlineAnswer(listOf(OutlineSymbol("TokenService", "class", CodeRange.onLine(40, 6, 12), children = listOf(OutlineSymbol("refresh", "function", CodeRange.onLine(47, 8, 7), listOf("token"), "TokenPair")))), Answers.byName)
        assertEquals(outline, json.decodeFromString<OutlineAnswer>(json.encodeToString(outline)))
    }

    @Test
    fun aSymbolHintRoundTripsThroughJson() {
        val doc = HintDoc(
            "kdoc", "Revokes every live token.", 20, 26, deprecated = "",
            sections = listOf(HintSection(HintSectionKind.Parameters, listOf(HintEntry("the family", 22, "familyId")))),
        )
        val hint = SymbolHint(Answers.target("a/T.kt", 28), listOf(HintLine("fun revoke(familyId: String): Int", 28)), "TokenFamilyService", "class", doc)
        val hover = HoverAnswer("fun revoke", Answers.byName, CodeRange.onLine(52, 19, 6), listOf(hint))
        assertEquals(hover, json.decodeFromString<HoverAnswer>(json.encodeToString(hover)))
    }

    @Test
    fun aPluginWritesPlainJson() {
        val text = """{"markdown":"Rotates on use","origin":{"provider":"auth-plugin","precision":"Exact"}}"""
        val hover = json.decodeFromString<HoverAnswer>(text)
        assertEquals("auth-plugin", hover.origin.provider)
        assertTrue(hover.range == null)
    }
}

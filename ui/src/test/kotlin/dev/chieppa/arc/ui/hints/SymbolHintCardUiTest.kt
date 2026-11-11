package dev.chieppa.arc.ui.hints

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import dev.chieppa.arc.codeintel.model.AnswerOrigin
import dev.chieppa.arc.codeintel.model.CodeLocation
import dev.chieppa.arc.codeintel.model.CodeRange
import dev.chieppa.arc.codeintel.model.Confidence
import dev.chieppa.arc.codeintel.model.DocumentRef
import dev.chieppa.arc.codeintel.model.HintDoc
import dev.chieppa.arc.codeintel.model.HintEntry
import dev.chieppa.arc.codeintel.model.HintLine
import dev.chieppa.arc.codeintel.model.HintSection
import dev.chieppa.arc.codeintel.model.HintSectionKind
import dev.chieppa.arc.codeintel.model.HoverAnswer
import dev.chieppa.arc.codeintel.model.Precision
import dev.chieppa.arc.codeintel.model.SymbolHint
import dev.chieppa.arc.codeintel.model.SymbolTarget
import dev.chieppa.arc.ui.shows
import dev.chieppa.arc.ui.testDispatchers
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.code.SymbolCard
import dev.chieppa.stretch.test.setStretchContent
import dev.chieppa.stretch.theme.StretchThemes
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class SymbolHintCardUiTest {
    private val origin = AnswerOrigin("tree-sitter", Precision.ByName, "kotlin")

    private fun hint(path: String, signature: String, doc: HintDoc?) = SymbolHint(
        SymbolTarget(CodeLocation(DocumentRef("api", "9c1e2fa", path, "kotlin"), CodeRange.onLine(28, 8, 6)), "revoke", "function", "", Confidence.Confident, origin, "api#412"),
        listOf(HintLine(signature, 28)),
        "TokenFamilyService",
        "class",
        doc,
    )

    private val documented = hint(
        "auth/TokenFamilyService.kt",
        "fun revoke(familyId: String): Int",
        HintDoc(
            "kdoc", "Revokes every live token in the family.", 20, 26, deprecated = "use the reason overload",
            sections = listOf(HintSection(HintSectionKind.Parameters, listOf(HintEntry("the family made at login", 22, "familyId")))),
        ),
    )
    private val bare = hint("legacy/TokenFamilyService.kt", "fun revoke(id: Long)", null)

    private class Card : SymbolCard {
        override var pinned by mutableStateOf(false)
        var closed = false

        override fun close() {
            closed = true
        }
    }

    @Test
    fun theCardShowsTheSignatureDocAndStepsBetweenDefinitions() = runComposeUiTest {
        val workbench = Workbench(dispatchers = testDispatchers())
        val card = Card()
        val answer = HoverAnswer("", origin, null, listOf(documented, bare))
        setStretchContent(StretchThemes.WorkshopDark) { SymbolHintCard(answer, card, workbench.hints) }
        waitForIdle()
        assertTrue(shows("fun revoke(familyId: String): Int"))
        assertTrue(shows("Revokes every live token"))
        assertTrue(shows("Parameters"))
        assertTrue(shows("familyId"))
        assertTrue(shows("Deprecated"))
        assertTrue(shows("1 / 2"))
        assertTrue(shows("KDoc · by name"))

        onNodeWithContentDescription("Keep open").performClick()
        assertTrue(card.pinned)

        onNodeWithContentDescription("Next definition").performClick()
        waitForIdle()
        assertTrue(shows("fun revoke(id: Long)"))
        assertTrue(shows("2 / 2"))
        assertFalse(shows("Revokes every live token"))
    }

    @Test
    fun anAnswerWithoutDefinitionsShowsItsMarkdown() = runComposeUiTest {
        val workbench = Workbench(dispatchers = testDispatchers())
        setStretchContent(StretchThemes.WorkshopDark) { SymbolHintCard(HoverAnswer("Rotates on use", origin), Card(), workbench.hints) }
        waitForIdle()
        assertTrue(shows("Rotates on use"))
    }
}

package dev.chieppa.arc.ui.hints

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import dev.chieppa.arc.codeintel.model.DocumentRef
import dev.chieppa.arc.codeintel.model.HintDoc
import dev.chieppa.arc.codeintel.model.HintEntry
import dev.chieppa.arc.codeintel.model.HintSection
import dev.chieppa.arc.codeintel.model.HintSectionKind
import dev.chieppa.arc.ui.shows
import dev.chieppa.arc.ui.testDispatchers
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.test.setStretchContent
import dev.chieppa.stretch.theme.StretchThemes
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class RenderedDocsUiTest {
    private val revoke = HintDoc(
        "kdoc", "Revokes every live token in the family.", 20, 26,
        sections = listOf(HintSection(HintSectionKind.Returns, listOf(HintEntry("how many tokens were revoked", 24)))),
    )
    private val open = HintDoc("kdoc", "The families a user has open.", 33, 33)

    @Test
    fun docsTouchingKeptLinesOrShownRawStayAsWritten() {
        assertEquals(listOf(open), DocBlocks.shown(listOf(revoke, open), kept = setOf(22), raw = emptySet()))
        assertEquals(listOf(revoke), DocBlocks.shown(listOf(revoke, open), kept = emptySet(), raw = setOf(33)))
        assertEquals(listOf(revoke, open), DocBlocks.shown(listOf(revoke, open), kept = setOf(27, 30), raw = emptySet()))
    }

    @Test
    fun aRenderedBlockShowsTheDocAndAClickAsksForItAsWritten() = runComposeUiTest {
        val workbench = Workbench(dispatchers = testDispatchers())
        var raw = 0
        setStretchContent(StretchThemes.WorkshopDark) {
            RenderedDocBlock(revoke, DocumentRef("api", "9c1e2fa", "auth/TokenFamilyService.kt", "kotlin"), workbench.hints) { raw++ }
        }
        waitForIdle()
        assertTrue(shows("Revokes every live token"))
        assertTrue(shows("Returns"))
        onNodeWithText("Returns").performClick()
        assertEquals(1, raw)
    }
}

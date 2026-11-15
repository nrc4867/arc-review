package dev.chieppa.arc.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performMouseInput
import androidx.compose.ui.test.v2.runComposeUiTest
import dev.chieppa.stretch.ui.TOOLTIP_DELAY_MS
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class PaneToolRowUiTest {
    private val data = FakeReviewData(more = true)

    private fun androidx.compose.ui.test.ComposeUiTest.openFile(path: String) = openReview(data).also {
        assertTrue(it.panes.openChange(data.source, path))
        waitForIdle()
    }

    /** Rests the pointer on the node and gives its tooltip time to show; leaving it takes the tooltip away again. */
    private fun androidx.compose.ui.test.ComposeUiTest.hover(node: androidx.compose.ui.test.SemanticsNodeInteraction, show: () -> Unit) {
        node.performMouseInput { moveTo(center) }
        mainClock.advanceTimeBy(TOOLTIP_DELAY_MS * 2)
        waitForIdle()
        show()
        node.performMouseInput { moveTo(androidx.compose.ui.geometry.Offset(-1f, -1f)) }
        mainClock.advanceTimeBy(TOOLTIP_DELAY_MS)
        waitForIdle()
    }

    @Test
    fun everyIconInTheRowNamesItselfInATooltipWithItsKey() = runComposeUiTest {
        openFile("app/Main.kt")
        val keyed = mapOf("Previous change" to "F7", "Next change" to "F7", "Find in file" to "F", "Wrap long lines" to "Z", "Rendered docs" to "D")
        for (label in listOf("Split", "Unified", "Previous change", "Next change", "Wrap long lines", "Rendered docs", "Find in file", "Pin")) {
            assertTrue(onAllNodesWithText(label).fetchSemanticsNodes().isEmpty(), "$label has no text until hovered")
            hover(onAllNodesWithContentDescription(label).onFirst()) {
                assertTrue(onAllNodesWithText(label).fetchSemanticsNodes().isNotEmpty(), "$label shows its tooltip")
                keyed[label]?.let { key -> assertTrue(shows(key), "$label's tooltip shows its key") }
            }
        }
    }

    @Test
    fun markReviewedShowsItsKeyOnlyInItsTooltip() = runComposeUiTest {
        openFile("app/Main.kt")
        assertFalse(shows("Mark reviewed and go to next"))
        assertTrue(onAllNodesWithText("R").fetchSemanticsNodes().isEmpty())
        hover(onNodeWithText("Mark reviewed")) {
            assertTrue(shows("Mark reviewed and go to next"))
            assertTrue(onAllNodesWithText("R").fetchSemanticsNodes().isNotEmpty())
        }
    }
}

package dev.chieppa.arc.ui

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class ImagePanesUiTest {
    private val data = FakeReviewData(more = true, pictures = true)

    private fun ComposeUiTest.openFile(path: String) = openReview(data).also {
        assertTrue(it.panes.openChange(data.source, path))
        waitForIdle()
    }

    private fun ComposeUiTest.described(description: String) =
        onAllNodesWithContentDescription(description, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()

    @Test
    fun aChangedPictureShowsBeforeAndAfterWithTheirSizes() = runComposeUiTest {
        openFile("img/photo.png")
        waitUntil { shows("30 × 20") }
        assertTrue(shows("40 × 20"))
        assertTrue(described("Before") && described("After"))
        assertFalse(shows("Binary file"))
    }

    @Test
    fun anSvgShowsDrawnAndAsItsText() = runComposeUiTest {
        openFile("img/icon.svg")
        waitUntil { shows("Rendered") }
        onNodeWithText("Rendered").performClick()
        waitUntil { shows("16 × 16") }
        onNodeWithText("Full file").performClick()
        waitUntil { shows("circle") }
    }

    @Test
    fun aBinaryFileThatIsNoPictureStillShowsItsKind() = runComposeUiTest {
        openFile("img/logo.png")
        waitUntil { shows("Binary file") }
    }
}

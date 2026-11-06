package dev.chieppa.arc.ui

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithText
import dev.chieppa.arc.ui.frame.ArcFrame
import dev.chieppa.arc.ui.review.ReviewState
import dev.chieppa.arc.ui.services.TextClipboard
import dev.chieppa.arc.ui.settings.UiPreferences
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.test.setStretchContent
import dev.chieppa.stretch.theme.StretchThemes
import java.nio.file.Path
import kotlin.test.assertTrue

/** What the tests copy to: the last text copied. */
class CopiedText : TextClipboard {
    var last: String? = null

    override fun copy(text: String) {
        last = text
    }
}

/** The main window on [data] with its source added and read, copying to [clipboard]. */
@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.openReview(
    data: FakeReviewData,
    clipboard: TextClipboard = CopiedText(),
    preferences: UiPreferences = UiPreferences(),
): Workbench {
    val workbench = Workbench(preferences = preferences, data = data, state = ReviewState(repos = mapOf(data.repo to Path.of("."))), clipboard = clipboard)
    setStretchContent(StretchThemes.WorkshopDark) { ArcFrame(workbench) }
    waitForIdle()
    assertTrue(workbench.addSource(data.source.ref))
    waitUntil { workbench.state.snapshot(data.source) != null }
    waitForIdle()
    return workbench
}

/** Whether any node shows [text], whole or as part of a longer text. */
@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.shows(text: String): Boolean = onAllNodesWithText(text, substring = true).fetchSemanticsNodes().isNotEmpty()

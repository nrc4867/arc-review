package dev.chieppa.arc.ui

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.withKeyDown
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import dev.chieppa.arc.ui.actions.ArcActionIds
import dev.chieppa.arc.ui.frame.ArcFrame
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.test.setStretchContent
import dev.chieppa.stretch.theme.StretchThemes
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class ArcFrameUiTest {
    private val drawerNames = listOf(
        "Files", "Outline", "Search", "Notes", "Conversation", "Revisions", "Console", "Pull requests", "Checks", "Staged comments",
    )

    @Test
    fun theFrameShowsTheAppAndEveryDrawerOnTheRails() = runComposeUiTest {
        setStretchContent(StretchThemes.WorkshopDark) { ArcFrame(Workbench()) }
        assertTrue(onAllNodesWithText("Arc").fetchSemanticsNodes().isNotEmpty())
        for (name in drawerNames) {
            assertTrue(onAllNodesWithContentDescription(name).fetchSemanticsNodes().isNotEmpty(), name)
        }
    }

    @Test
    fun goingToAnythingOpensThePaletteOfCommands() = runComposeUiTest {
        val workbench = Workbench()
        setStretchContent(StretchThemes.WorkshopDark) { ArcFrame(workbench) }
        workbench.registry[ArcActionIds.GO_TO_ANYTHING]!!.run()
        waitForIdle()
        assertTrue(onAllNodesWithText("Reset layout").fetchSemanticsNodes().isNotEmpty())
    }

    @Test
    fun ctrlKOrCmdKFromTheKeyboardOpensThePalette() = runComposeUiTest {
        val workbench = Workbench()
        setStretchContent(StretchThemes.WorkshopDark) { ArcFrame(workbench) }
        waitForIdle()
        onRoot().performKeyInput { withKeyDown(shortcutKey) { pressKey(Key.K) } }
        waitForIdle()
        assertTrue(workbench.paletteOpen)
    }
}

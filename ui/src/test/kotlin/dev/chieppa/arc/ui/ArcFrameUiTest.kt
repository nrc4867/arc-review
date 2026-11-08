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
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runDesktopComposeUiTest
import kotlin.math.abs
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class ArcFrameUiTest {
    private companion object {
        const val WIDE = 1440
        const val TALL = 900
    }

    private val drawerNames = listOf(
        "Files", "Outline", "Search", "Notes", "Conversation", "Revisions", "Console", "Pull requests", "Checks", "Staged comments",
    )

    @Test
    fun theFrameShowsTheAppAndEveryDrawerOnTheRails() = runComposeUiTest {
        val workbench = Workbench(dispatchers = testDispatchers())
        setStretchContent(StretchThemes.WorkshopDark) { ArcFrame(workbench) }
        assertTrue(onAllNodesWithText("Arc").fetchSemanticsNodes().isNotEmpty())
        for (name in drawerNames) {
            assertTrue(onAllNodesWithContentDescription(name).fetchSemanticsNodes().isNotEmpty(), name)
        }
    }

    @Test
    fun commandsOpenThePaletteOfCommands() = runComposeUiTest {
        val workbench = Workbench(dispatchers = testDispatchers())
        setStretchContent(StretchThemes.WorkshopDark) { ArcFrame(workbench) }
        workbench.registry[ArcActionIds.COMMANDS]!!.run()
        waitForIdle()
        assertTrue(onAllNodesWithText("Reset layout").fetchSemanticsNodes().isNotEmpty())
    }

    @Test
    fun ctrlKOrCmdKFromTheKeyboardOpensTheSwitcher() = runComposeUiTest {
        val workbench = Workbench(dispatchers = testDispatchers())
        setStretchContent(StretchThemes.WorkshopDark) { ArcFrame(workbench) }
        waitForIdle()
        onRoot().performKeyInput { withKeyDown(shortcutKey) { pressKey(Key.K) } }
        waitForIdle()
        assertTrue(workbench.switcher.open)
    }

    @Test
    fun theTopBarFieldOpensTheSwitcher() = runComposeUiTest {
        val workbench = Workbench(dispatchers = testDispatchers())
        setStretchContent(StretchThemes.WorkshopDark) { ArcFrame(workbench) }
        onNodeWithContentDescription("Open switcher").performClick()
        waitForIdle()
        assertTrue(workbench.switcher.open)
    }

    @Test
    fun goToAnythingSitsInTheMiddleOfTheWholeBar() = runDesktopComposeUiTest(WIDE, TALL) {
        val workbench = Workbench(dispatchers = testDispatchers())
        setStretchContent(StretchThemes.WorkshopDark) { ArcFrame(workbench) }
        waitForIdle()
        val root = onRoot().getBoundsInRoot()
        val field = onNodeWithText("Go to anything").getBoundsInRoot()
        val middle = (field.left + field.right) / 2
        val rootMiddle = (root.left + root.right) / 2
        assertTrue(abs((middle - rootMiddle).value) <= 1f, "field centre $middle, bar centre $rootMiddle")
    }
}

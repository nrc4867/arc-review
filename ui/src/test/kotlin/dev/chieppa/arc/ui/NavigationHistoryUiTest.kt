package dev.chieppa.arc.ui

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.MouseButton
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performMouseInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.withKeyDown
import androidx.compose.ui.test.v2.runComposeUiTest
import dev.chieppa.arc.core.pane.PaneView
import dev.chieppa.arc.ui.actions.ArcActionIds
import dev.chieppa.arc.ui.navigation.NavReveal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class NavigationHistoryUiTest {
    private val data = FakeReviewData()

    @Test
    fun backAndForwardGoBetweenTheFilesOpened() = runComposeUiTest {
        val workbench = openReview(data)
        workbench.panes.openChange(data.source, "app/Main.kt")
        waitForIdle()
        workbench.panes.openChange(data.source, "app/util/Text.kt", newTab = true)
        waitForIdle()
        assertEquals("app/util/Text.kt", workbench.panes.focused?.path)

        workbench.registry[ArcActionIds.NAVIGATE_BACK]!!.run()
        waitForIdle()
        assertEquals("app/Main.kt", workbench.panes.focused?.path)

        workbench.registry[ArcActionIds.NAVIGATE_FORWARD]!!.run()
        waitForIdle()
        assertEquals("app/util/Text.kt", workbench.panes.focused?.path)
    }

    @Test
    fun theMouseBackButtonAndTheBackKeyGoBack() = runComposeUiTest {
        val workbench = openReview(data)
        workbench.panes.openChange(data.source, "app/Main.kt")
        waitForIdle()
        workbench.panes.openChange(data.source, "app/util/Text.kt", newTab = true)
        waitForIdle()

        onRoot().performMouseInput {
            moveTo(center)
            press(MouseButton(BACK_BUTTON))
            release(MouseButton(BACK_BUTTON))
        }
        waitForIdle()
        assertEquals("app/Main.kt", workbench.panes.focused?.path)

        onRoot().performMouseInput {
            press(MouseButton(FORWARD_BUTTON))
            release(MouseButton(FORWARD_BUTTON))
        }
        waitForIdle()
        assertEquals("app/util/Text.kt", workbench.panes.focused?.path)

        val mac = System.getProperty("os.name").orEmpty().startsWith("Mac")
        onRoot().performKeyInput {
            if (mac) withKeyDown(Key.MetaLeft) { pressKey(Key.LeftBracket) } else withKeyDown(Key.AltLeft) { pressKey(Key.DirectionLeft) }
        }
        waitForIdle()
        assertEquals("app/Main.kt", workbench.panes.focused?.path)
    }

    @Test
    fun aJumpIntoAFoldedLineOpensTheFoldAndGoingBackReturns() = runComposeUiTest {
        val workbench = openReview(data)
        workbench.panes.openChange(data.source, "app/Main.kt")
        waitForIdle()
        assertEquals(PaneView.Diff, workbench.panes.focused?.view)
        assertTrue(onAllNodesWithText("line 5", substring = false).fetchSemanticsNodes().isEmpty())

        workbench.navigation.reveal = NavReveal(data.repo, data.head.value, "app/Main.kt", 5, 0, end = 4)
        waitForIdle()
        assertFalse(onAllNodesWithText("line 5", substring = false).fetchSemanticsNodes().isEmpty())
        assertEquals(1, workbench.history.history.behind.size)
        assertEquals(5, workbench.navigation.caret?.line)
    }

    @Test
    fun aMiddleClickOnASourceTakesItOut() = runComposeUiTest {
        val workbench = openReview(data)
        onNodeWithContentDescription(data.source.ref).performMouseInput {
            moveTo(center)
            press(MouseButton.Tertiary)
            release(MouseButton.Tertiary)
        }
        waitForIdle()
        assertTrue(workbench.state.workspace.sources.isEmpty())
    }

    private companion object {
        /** The mouse's back and forward buttons as Compose numbers them from 0. */
        const val BACK_BUTTON = 3
        const val FORWARD_BUTTON = 4
    }
}

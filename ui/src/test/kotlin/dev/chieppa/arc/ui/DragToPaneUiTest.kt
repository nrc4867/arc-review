package dev.chieppa.arc.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performMouseInput
import androidx.compose.ui.test.runDesktopComposeUiTest
import dev.chieppa.arc.ui.tree.TreeKeys
import dev.chieppa.stretch.frame.PaneItemDrop
import dev.chieppa.stretch.frame.PaneLeaf
import dev.chieppa.stretch.frame.PaneSplit
import dev.chieppa.stretch.frame.WindowBounds
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class DragToPaneUiTest {
    private val data = FakeReviewData()

    /** Drags [from] with the mouse to [to], in dp from the root, in steps, and lets go. */
    private fun ComposeUiTest.mouseDrag(from: SemanticsNodeInteraction, to: Offset) {
        val bounds = from.getUnclippedBoundsInRoot()
        val start = Offset(((bounds.left + bounds.right) / 2).value, ((bounds.top + bounds.bottom) / 2).value)
        from.performMouseInput {
            moveTo(center)
            press()
            val step = (to - start) * density / STEPS.toFloat()
            repeat(STEPS) { moveBy(step) }
        }
        waitForIdle()
        from.performMouseInput { release() }
        waitForIdle()
    }

    /** The open pane's body, from its header's path to the right rail, in dp from the root. */
    private fun ComposeUiTest.paneSpan(): Triple<Float, Float, Float> {
        val root = onRoot().getUnclippedBoundsInRoot()
        val left = onAllNodesWithText("app/util/Text.kt", substring = true).onFirst().getUnclippedBoundsInRoot().left.value
        val right = root.right.value - RAIL
        return Triple(left, right, ((root.top + root.bottom) / 2).value)
    }

    private fun ComposeUiTest.openWithAPane() = openReview(data).also {
        it.panes.openChange(data.source, "app/util/Text.kt")
        waitForIdle()
    }

    @Test
    fun aFileDraggedOntoAPanesMiddleOpensAsATabThere() = runDesktopComposeUiTest(WIDE, TALL) {
        val workbench = openWithAPane()
        val (left, right, middle) = paneSpan()
        mouseDrag(onAllNodesWithText("Main.kt")[0], Offset((left + right) / 2, middle))
        assertEquals(1, workbench.paneLayout.leaves.size)
        assertEquals(2, workbench.paneLayout.tabs.size)
        assertEquals("app/Main.kt", workbench.panes.focused?.path)
    }

    @Test
    fun aFileDraggedOntoAPanesEdgeSplitsIt() = runDesktopComposeUiTest(WIDE, TALL) {
        val workbench = openWithAPane()
        val (_, right, middle) = paneSpan()
        mouseDrag(onAllNodesWithText("Main.kt")[0], Offset(right - EDGE, middle))
        assertTrue(workbench.paneLayout.root is PaneSplit)
        assertEquals(listOf(listOf("app/util/Text.kt"), listOf("app/Main.kt")), workbench.paneLayout.leaves.map { leaf -> leaf.tabs.map { workbench.state.pane(it)!!.path } })
    }

    @Test
    fun aFileDroppedAwayFromTheGridOpensInAWindowOfItsOwn() = runDesktopComposeUiTest(WIDE, TALL) {
        val workbench = openWithAPane()
        workbench.dropFile(TreeKeys.file(data.source, "app/Main.kt"), PaneItemDrop.InWindow(WindowBounds(10f, 10f, 400f, 300f)))
        val window = workbench.paneLayout.windows.single()
        assertEquals(listOf("app/Main.kt"), (window.root as PaneLeaf).tabs.map { workbench.state.pane(it)!!.path })
    }

    private companion object {
        const val WIDE = 1440
        const val TALL = 900
        const val STEPS = 20
        const val RAIL = 60f
        const val EDGE = 24f
    }
}

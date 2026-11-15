package dev.chieppa.arc.ui.files

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performMouseInput
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import dev.chieppa.arc.core.change.CommitInfo
import dev.chieppa.arc.core.change.ObjectId
import dev.chieppa.stretch.test.setStretchContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class ScopeMenuUiTest {
    private val commits = (1..60).map { n -> CommitInfo(ObjectId("%040x".format(n)), "Commit number $n", "Test", n.toLong()) }

    private fun androidx.compose.ui.test.ComposeUiTest.listed() = onAllNodesWithText("Commit number 1", substring = true).fetchSemanticsNodes().size

    @Test
    fun pressingTheScopeWhileItsListIsOpenClosesIt() = runComposeUiTest {
        setStretchContent { Box(Modifier.width(300.dp)) { ScopeMenu(commits, null, {}) } }
        val scope = onNodeWithContentDescription("Scope")
        // A person's press and release come in separate frames, so the list closing on the press has recomposed by the release.
        fun click() {
            scope.performMouseInput { moveTo(center); press() }
            waitForIdle()
            scope.performMouseInput { release() }
            waitForIdle()
        }
        click()
        assertEquals(true, listed() > 0)
        click()
        assertEquals(0, listed())
        click()
        assertEquals(true, listed() > 0)
    }
}

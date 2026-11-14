package dev.chieppa.arc.ui.update

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import dev.chieppa.arc.ui.frame.ArcFrame
import dev.chieppa.arc.ui.plugins.PluginRig.Companion.shows
import dev.chieppa.arc.ui.testDispatchers
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.test.setStretchContent
import dev.chieppa.stretch.theme.StretchThemes
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class UpdateUiTest {
    private class FakeUpdates(override val previous: String? = null) : AppUpdates {
        override val version = "0.2.0"
        var newer: String? = "0.3.0"
        var failing = false
        var checkFailure: String? = null
        val downloaded = CompletableDeferred<Unit>()
        val calls = mutableListOf<String>()

        override suspend fun check(): String? {
            checkFailure?.let { throw IOException(it) }
            return newer
        }

        override suspend fun prepare(progress: (Long, Long?) -> Unit) {
            calls += "prepare"
            progress(50, 100)
            downloaded.await()
            if (failing) throw IOException("offline")
        }

        override suspend fun activate() {
            calls += "activate"
        }

        override suspend fun switchBack() {
            calls += "switchBack"
        }
    }

    private var quits = 0

    @Test
    fun aNewerReleaseIsDownloadedFromTheStatusBarAndRestartsArc() = runComposeUiTest {
        val updates = FakeUpdates()
        val workbench = Workbench(onQuit = { quits++ }, dispatchers = testDispatchers(), updates = updates)
        setStretchContent(StretchThemes.WorkshopDark) { ArcFrame(workbench) }
        waitForIdle()
        assertFalse(shows("Update to 0.3.0"))
        workbench.scope.launch { workbench.updates.check() }
        waitForIdle()
        onNodeWithText("Update to 0.3.0").performClick()
        waitForIdle()
        assertTrue(shows("Updating to 0.3.0, 50%"))
        updates.downloaded.complete(Unit)
        waitForIdle()
        onNodeWithText("Restart to update").performClick()
        waitForIdle()
        assertEquals(listOf("prepare", "activate"), updates.calls)
        assertEquals(1, quits)
    }

    @Test
    fun aFailedCheckShowsWhyInSettingsAndIsTriedAgainThere() = runComposeUiTest {
        val updates = FakeUpdates().apply { checkFailure = "No GitHub token" }
        val workbench = Workbench(onQuit = { quits++ }, dispatchers = testDispatchers(), updates = updates)
        setStretchContent(StretchThemes.WorkshopDark) { ArcFrame(workbench) }
        workbench.scope.launch { workbench.updates.check() }
        waitForIdle()
        assertEquals("No GitHub token", workbench.updates.problem)
        assertEquals(UpdateState.Current, workbench.updates.state)
        updates.checkFailure = null
        workbench.updates.press()
        waitForIdle()
        assertEquals(null, workbench.updates.problem)
        assertEquals(UpdateState.Available("0.3.0"), workbench.updates.state)
        assertTrue(shows("Update to 0.3.0"))
    }

    @Test
    fun aFailedDownloadStaysToBeTriedAgain() = runComposeUiTest {
        val updates = FakeUpdates().apply { failing = true; downloaded.complete(Unit) }
        val workbench = Workbench(onQuit = { quits++ }, dispatchers = testDispatchers(), updates = updates)
        setStretchContent(StretchThemes.WorkshopDark) { ArcFrame(workbench) }
        workbench.scope.launch { workbench.updates.check() }
        waitForIdle()
        onNodeWithText("Update to 0.3.0").performClick()
        waitForIdle()
        assertEquals(UpdateState.Failed("0.3.0"), workbench.updates.state)
        assertTrue(shows("Update to 0.3.0"))
        assertEquals(0, quits)
    }

    @Test
    fun settingsShowTheVersionTheUpdateAndSwitchingBack() = runComposeUiTest {
        val updates = FakeUpdates(previous = "0.1.0")
        val workbench = Workbench(onQuit = { quits++ }, dispatchers = testDispatchers(), updates = updates)
        setStretchContent(StretchThemes.WorkshopDark) { ArcFrame(workbench) }
        workbench.scope.launch { workbench.updates.check() }
        workbench.panes.openSettings()
        waitForIdle()
        assertTrue(shows("Updates"))
        assertTrue(shows("0.2.0"))
        onNodeWithText("Switch back to 0.1.0").performClick()
        waitForIdle()
        assertEquals(listOf("switchBack"), updates.calls)
        assertEquals(1, quits)
    }

    @Test
    fun withoutUpdatesThereIsNoCard() = runComposeUiTest {
        val workbench = Workbench(dispatchers = testDispatchers())
        setStretchContent(StretchThemes.WorkshopDark) { ArcFrame(workbench) }
        workbench.panes.openSettings()
        waitForIdle()
        assertFalse(shows("Updates"))
    }
}

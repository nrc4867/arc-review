package dev.chieppa.arc.ui

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import dev.chieppa.arc.core.activity.ActivityChannel
import dev.chieppa.arc.core.activity.ActivityEntry
import dev.chieppa.arc.core.activity.ActivityKind
import dev.chieppa.arc.core.activity.ActivityOutcome
import dev.chieppa.arc.core.watch.SavedFilter
import dev.chieppa.arc.core.watch.Watchlist
import dev.chieppa.arc.core.workspace.Workspace
import dev.chieppa.arc.ui.FakePulls.pull
import dev.chieppa.arc.ui.activity.ActivityFilter
import dev.chieppa.arc.ui.drawers.DrawerIds
import dev.chieppa.arc.ui.frame.ArcFrame
import dev.chieppa.arc.ui.pulls.FilterResult
import dev.chieppa.arc.ui.pulls.PullWatch
import dev.chieppa.arc.ui.review.WorkspaceState
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.arc.ui.workbench.openConsole
import dev.chieppa.stretch.test.setStretchContent
import dev.chieppa.stretch.theme.StretchThemes
import java.nio.file.Path
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class ConsoleUiTest {
    private val data = FakeReviewData()
    private val reason = "Cloning into 'api'...\nfatal: could not read Username for 'https://github.com': terminal prompts disabled"
    private val watch = PullWatch(Watchlist(listOf(SavedFilter("f1", "Team: auth", "org:lumen")))).apply {
        results = mapOf("f1" to FilterResult(1, listOf(pull(412, "Rotate refresh tokens on every use")), System.currentTimeMillis()))
    }

    private fun ComposeUiTest.open(): Workbench {
        val workbench = Workbench(
            dispatchers = testDispatchers(),
            data = data,
            state = WorkspaceState(Workspace(), repos = mapOf(data.repo to Path.of("."))),
            watch = watch,
        )
        setStretchContent(StretchThemes.WorkshopDark) { ArcFrame(workbench) }
        waitForIdle()
        return workbench
    }

    private fun entry(title: String, outcome: ActivityOutcome, error: String? = null) =
        ActivityEntry(ActivityKind.Git, title, outcome, Instant.EPOCH, 12, "C:/clones/lumen", if (error != null) "exit 128" else null, error)

    private fun Workbench.consoleOpen() = frameLayout.drawers.first { it.id == DrawerIds.CONSOLE }.open

    @Test
    fun theConsoleShowsEveryOperationAndItsFailuresWithWhy() = runComposeUiTest {
        val workbench = open()
        workbench.console.log.report(entry("git rev-parse HEAD", ActivityOutcome.Succeeded))
        workbench.openConsole()
        waitForIdle()
        workbench.console.log.report(entry("git fetch --quiet origin", ActivityOutcome.Failed, "fatal: Authentication failed for 'https://github.com/lumen/api.git/'"))
        waitForIdle()
        assertTrue(shows("git rev-parse HEAD"))
        assertTrue(shows("git fetch --quiet origin"))
        assertTrue(shows("fatal: Authentication failed for 'https://github.com/lumen/api.git/'"))
        assertTrue(shows("12 ms"))
        assertTrue(shows("failed exit 128"))
    }

    @Test
    fun errorsOnlyLeavesOutWhatSucceeded() = runComposeUiTest {
        val workbench = open()
        workbench.console.log.report(entry("git rev-parse HEAD", ActivityOutcome.Succeeded))
        workbench.console.log.report(entry("git fetch --quiet origin", ActivityOutcome.Failed, "fatal: no"))
        workbench.openConsole()
        waitForIdle()
        onNodeWithText("Errors").performClick()
        waitForIdle()
        assertEquals(ActivityFilter.Errors, workbench.console.filter)
        assertFalse(shows("git rev-parse HEAD"))
        assertTrue(shows("git fetch --quiet origin"))
        onNodeWithText("All").performClick()
        waitForIdle()
        assertTrue(shows("git rev-parse HEAD"))
    }

    @Test
    fun eachPluginWritesToAChannelOfItsOwnShownAsPlainText() = runComposeUiTest {
        val workbench = open()
        val todos = ActivityChannel.plugin("todos", "TODOs")
        workbench.console.log.report(entry("git rev-parse HEAD", ActivityOutcome.Succeeded))
        workbench.console.log.report(ActivityEntry(ActivityKind.Output, "[31mScanned 3 files", ActivityOutcome.Succeeded, Instant.EPOCH, channel = todos))
        workbench.openConsole()
        waitForIdle()
        assertTrue(shows("TODOs  [31mScanned 3 files"))
        onNodeWithContentDescription("Channel").performClick()
        waitForIdle()
        onNodeWithText("TODOs").performClick()
        waitForIdle()
        assertEquals(todos.id, workbench.console.channel)
        assertTrue(shows("[31mScanned 3 files"))
        assertFalse(shows("git rev-parse HEAD"))
        onNodeWithContentDescription("Clear console").performClick()
        waitForIdle()
        assertTrue(workbench.console.log.entries(todos.id).isEmpty())
        assertEquals(1, workbench.console.log.entries().size)
    }

    @Test
    fun clearingEmptiesTheConsole() = runComposeUiTest {
        val workbench = open()
        workbench.console.log.report(entry("git rev-parse HEAD", ActivityOutcome.Succeeded))
        workbench.openConsole()
        waitForIdle()
        onNodeWithContentDescription("Clear console").performClick()
        waitForIdle()
        assertFalse(shows("git rev-parse HEAD"))
        assertTrue(workbench.console.log.entries().isEmpty())
    }

    @Test
    fun aRepoThatCannotBeClonedSaysWhyAndOpensTheConsoleOnItsErrors() = runComposeUiTest {
        data.cloneError = reason
        val workbench = open()
        workbench.panes.openFilter("f1")
        waitForIdle()
        onNodeWithText("Rotate refresh tokens on every use").performClick()
        waitForIdle()
        assertEquals(mapOf(FakePulls.api to reason), workbench.cloneFailed)
        val mark = "Could not clone lumen/api: fatal: could not read Username for 'https://github.com': terminal prompts disabled"
        onNodeWithContentDescription(mark).performClick()
        waitForIdle()
        assertTrue(workbench.consoleOpen())
        assertEquals(ActivityFilter.Errors, workbench.console.filter)
    }
}

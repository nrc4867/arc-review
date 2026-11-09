package dev.chieppa.arc.ui

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.test.withKeyDown
import dev.chieppa.arc.core.source.BranchName
import dev.chieppa.arc.core.source.CommitSha
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.git.repo.LocalBranch
import dev.chieppa.arc.host.github.repo.GitHubRepo
import dev.chieppa.arc.ui.frame.ArcFrame
import dev.chieppa.arc.ui.review.WorkspaceState
import dev.chieppa.arc.ui.switcher.SwitcherLimits
import dev.chieppa.arc.ui.switcher.SwitcherScope
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.test.setStretchContent
import dev.chieppa.stretch.theme.StretchThemes
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class SwitcherUiTest {
    private val data = FakeReviewData()
    private val other = Source.Commit(data.repo, CommitSha("c".repeat(40)))
    private val field = "Switch to a source, file or pull request, or paste a link"
    private val sourcesField = "Find a pull request, branch or recent source, or paste a link or a repo folder"

    private fun ComposeUiTest.open(): Workbench {
        val workbench = Workbench(dispatchers = testDispatchers(), data = data, state = WorkspaceState(repos = mapOf(data.repo to Path.of("."))))
        setStretchContent(StretchThemes.WorkshopDark) { ArcFrame(workbench) }
        waitForIdle()
        assertTrue(workbench.addSource(other.ref))
        assertTrue(workbench.addSource(data.source.ref))
        assertWhenIdle { workbench.state.snapshot(data.source) != null && workbench.state.snapshot(other) != null }
        waitForIdle()
        return workbench
    }

    private fun ComposeUiTest.search(text: String) {
        onRoot().performKeyInput { withKeyDown(shortcutKey) { pressKey(Key.K) } }
        waitForIdle()
        onNodeWithContentDescription(field).performTextInput(text)
        waitForIdle()
    }

    private fun ComposeUiTest.press(key: Key, modifier: Key? = null, on: String = field) {
        onNodeWithContentDescription(on).performKeyInput { if (modifier == null) pressKey(key) else withKeyDown(modifier) { pressKey(key) } }
        waitForIdle()
    }

    @Test
    fun enterSwitchesToTheSourceFound() = runComposeUiTest {
        val workbench = open()
        assertEquals(data.source, workbench.state.active)
        search("demo@c")
        assertTrue(shows("OPEN"))
        assertTrue(shows("1 result"))
        press(Key.Enter)
        assertEquals(other, workbench.state.active)
        assertFalse(workbench.switcher.open)
    }

    @Test
    fun tabMovesThroughTheScopes() = runComposeUiTest {
        val workbench = open()
        search("main")
        assertTrue(shows("FILES IN"))
        press(Key.Tab)
        assertEquals(SwitcherScope.Sources, workbench.switcher.scope)
        press(Key.Tab, on = sourcesField)
        assertEquals(SwitcherScope.Files, workbench.switcher.scope)
    }

    @Test
    fun ctrlEnterOpensAFileInANewPane() = runComposeUiTest {
        val workbench = open()
        search("Text.kt")
        press(Key.Enter, shortcutKey)
        assertWhenIdle { workbench.panes.focused?.path == "app/util/Text.kt" }
        val first = workbench.panes.focused!!.id
        search("Main.kt")
        press(Key.Enter, shortcutKey)
        assertWhenIdle { workbench.panes.focused?.path == "app/Main.kt" }
        assertTrue(workbench.state.pane(first.value) != null)
    }

    @Test
    fun followedBranchesShowAndAltEnterOpensOneWithoutSwitching() = runComposeUiTest {
        data.branchList = listOf(LocalBranch("spike/token-binding", "mfischer", 0))
        val workbench = open()
        search("binding")
        assertTrue(shows("FOLLOWED REPOS"))
        assertTrue(shows("not open"))
        assertTrue(shows("mfischer"))
        press(Key.Enter, Key.AltLeft)
        val branch = Source.BranchAgainstBase(data.repo, BranchName("spike/token-binding"))
        assertTrue(branch in workbench.state.workspace.sources)
        assertEquals(data.source, workbench.state.active)
    }

    @Test
    fun openPullRequestsInFollowedReposShowWhenGitHubAnswers() = runComposeUiTest {
        data.origin = GitHubRepo("lumen", "demo")
        data.openPullList = listOf(FakePulls.pull(398, "Remove legacy token table", repo = GitHubRepo("lumen", "demo")))
        open()
        search("legacy")
        mainClock.advanceTimeBy(SwitcherLimits.DEBOUNCE_MILLIS)
        assertWhenIdle { shows("Remove legacy token table") }
        assertTrue(shows("demo#398"))
    }

    @Test
    fun theAddButtonSuggestsSourcesBeforeAnythingIsTyped() = runComposeUiTest {
        data.branchList = listOf(LocalBranch("spike/token-binding", "mfischer", 0))
        val workbench = open()
        onNodeWithContentDescription("Add source").performClick()
        waitForIdle()
        assertEquals(SwitcherScope.Sources, workbench.switcher.scope)
        assertWhenIdle { shows("SUGGESTED") }
        assertTrue(shows("spike/token-binding"))
        onNodeWithContentDescription(sourcesField).performTextInput("tokn bindng")
        waitForIdle()
        assertTrue(shows("FOLLOWED REPOS"))
        press(Key.Enter, on = sourcesField)
        assertEquals(Source.BranchAgainstBase(data.repo, BranchName("spike/token-binding")), workbench.state.active)
    }

    @Test
    fun aPastedFolderWithNoRepoIsMarkedInPlace() = runComposeUiTest {
        data.repoAt = false
        val workbench = open()
        workbench.switcher.show(SwitcherScope.Sources)
        waitForIdle()
        val folder = Path.of("").toAbsolutePath().toString()
        onNodeWithContentDescription(sourcesField).performTextInput(folder)
        waitForIdle()
        assertTrue(shows("Add repository"))
        press(Key.Enter, on = sourcesField)
        assertTrue(workbench.switcher.open)
        assertEquals(1, workbench.state.repos.size)
    }

    @Test
    fun aPastedLinkShowsWhatItPointsAtAndOpensIt() = runComposeUiTest {
        data.origin = GitHubRepo("lumen", "demo")
        val workbench = open()
        assertWhenIdle { workbench.gitHubRepos.isNotEmpty() }
        search("https://github.com/lumen/demo/commit/dddd1234")
        assertTrue(shows("GitHub commit"))
        assertTrue(shows("OPEN AS"))
        assertTrue(shows("Commit dddd123"))
        assertFalse(shows("New review"))
        press(Key.Enter)
        val commit = Source.Commit(data.repo, CommitSha("dddd1234"))
        assertTrue(commit in workbench.state.workspace.sources)
        assertEquals(commit, workbench.state.active)
    }
}

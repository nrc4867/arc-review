package dev.chieppa.arc.ui

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performMouseInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.rightClick
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.test.withKeyDown
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.host.github.GitHubPages
import dev.chieppa.arc.host.github.repo.GitHubRepo
import dev.chieppa.arc.ui.actions.ArcActionIds
import dev.chieppa.arc.ui.util.SourceLabels
import dev.chieppa.arc.ui.workbench.MenuTarget
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.core.ActionId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class OpenOnGitHubUiTest {
    private val github = GitHubRepo("acme", "demo")
    private val opened = mutableListOf<String>()

    private fun ComposeUiTest.open(data: FakeReviewData = FakeReviewData()): Workbench {
        data.origin = github
        val workbench = openReview(data, links = { opened += it })
        assertWhenIdle { workbench.gitHubRepos.isNotEmpty() }
        return workbench
    }

    private fun Workbench.run(id: ActionId, target: MenuTarget? = null) = withTarget(target) { registry[id]!!.run() }

    private fun Workbench.available(id: ActionId, target: MenuTarget? = null) = withTarget(target) { registry[id]!!.isAvailable() }

    @Test
    fun theActiveSourceOpensFromThePaletteAndItsKey() = runComposeUiTest {
        val data = FakeReviewData()
        val workbench = open(data)
        val compare = "https://github.com/acme/demo/compare/${data.base.value}..${data.head.value}"
        workbench.run(ArcActionIds.OPEN_ON_GITHUB)
        assertEquals(listOf(compare), opened)
        onRoot().performKeyInput { withKeyDown(shortcutKey) { withKeyDown(Key.ShiftLeft) { pressKey(Key.G) } } }
        waitForIdle()
        assertEquals(listOf(compare, compare), opened)
    }

    @Test
    fun aPullRequestOpensItsPageAndItsFilesInTheFilesView() = runComposeUiTest {
        val data = FakeReviewData()
        val workbench = open(data)
        val pull = Source.PullRequest(data.repo, 7)
        assertTrue(workbench.addSource(pull.ref))
        assertWhenIdle { workbench.state.snapshot(pull) != null }
        workbench.run(ArcActionIds.OPEN_ON_GITHUB, MenuTarget.OfSource(pull))
        assertEquals("https://github.com/acme/demo/pull/7", opened.last())

        assertTrue(workbench.panes.openChange(pull, "app/Main.kt"))
        waitForIdle()
        val pane = workbench.panes.focused!!.id.value
        workbench.state.paneLines[pane] = 20
        workbench.run(ArcActionIds.OPEN_FILE_ON_GITHUB, MenuTarget.OfPane(pane))
        assertEquals(GitHubPages.pullFile(github, 7, "app/Main.kt", 20), opened.last())
        workbench.run(ArcActionIds.OPEN_ON_GITHUB, MenuTarget.OfPane(pane))
        assertEquals("https://github.com/acme/demo/pull/7", opened.last())
    }

    @Test
    fun aFileOfACommitRangeOpensAtTheHeadAndADeletedOneAtTheBase() = runComposeUiTest {
        val data = FakeReviewData(more = true)
        val workbench = open(data)
        assertTrue(workbench.panes.openChange(data.source, "app/Main.kt"))
        waitForIdle()
        workbench.state.paneLines[workbench.panes.focused!!.id.value] = 1
        workbench.run(ArcActionIds.OPEN_FILE_ON_GITHUB)
        assertEquals("https://github.com/acme/demo/blob/${data.head.value}/app/Main.kt", opened.last())
        workbench.state.paneLines[workbench.panes.focused!!.id.value] = 12
        workbench.run(ArcActionIds.OPEN_FILE_ON_GITHUB)
        assertEquals("https://github.com/acme/demo/blob/${data.head.value}/app/Main.kt#L12", opened.last())

        assertTrue(workbench.panes.openChange(data.source, "old/Gone.kt"))
        waitForIdle()
        workbench.run(ArcActionIds.OPEN_FILE_ON_GITHUB)
        assertEquals("https://github.com/acme/demo/blob/${data.base.value}/old/Gone.kt", opened.last())
    }

    @Test
    fun theSourcesMenusOfferIt() = runComposeUiTest {
        val data = FakeReviewData()
        val workbench = open(data)
        workbench.panes.openSettings()
        waitForIdle()
        onNodeWithText(SourceLabels.of(data.source) + "  Rotate tokens").performMouseInput { rightClick() }
        waitForIdle()
        onNodeWithText("Open on GitHub").performClick()
        waitForIdle()
        assertEquals("https://github.com/acme/demo/compare/${data.base.value}..${data.head.value}", opened.single())
        onNodeWithText("Main.kt").performMouseInput { rightClick() }
        waitForIdle()
        onNodeWithText("Open file on GitHub").performClick()
        assertEquals("https://github.com/acme/demo/blob/${data.head.value}/app/Main.kt", opened.last())
    }

    @Test
    fun aRepoNotFromGitHubOffersNothing() = runComposeUiTest {
        val data = FakeReviewData()
        val workbench = openReview(data, links = { opened += it })
        assertTrue(workbench.panes.openChange(data.source, "app/Main.kt"))
        waitForIdle()
        assertFalse(workbench.available(ArcActionIds.OPEN_ON_GITHUB))
        assertFalse(workbench.available(ArcActionIds.OPEN_FILE_ON_GITHUB))
        assertFalse(workbench.available(ArcActionIds.OPEN_ON_GITHUB, MenuTarget.OfSource(data.source)))
    }
}

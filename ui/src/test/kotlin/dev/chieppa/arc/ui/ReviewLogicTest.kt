package dev.chieppa.arc.ui

import dev.chieppa.arc.core.change.ChangeStatus
import dev.chieppa.arc.core.change.ChangedFile
import dev.chieppa.arc.core.mark.FileReviewState
import dev.chieppa.arc.core.pane.Revision
import dev.chieppa.arc.core.source.SourceRefs
import dev.chieppa.arc.ui.find.FindMatcher
import dev.chieppa.arc.ui.find.FindQuery
import dev.chieppa.arc.ui.review.MarkController
import dev.chieppa.arc.ui.review.PaneController
import dev.chieppa.arc.ui.review.RepoNames
import dev.chieppa.arc.ui.review.WorkspaceState
import dev.chieppa.arc.ui.review.SourceLoad
import dev.chieppa.arc.ui.review.SourceSwitching
import dev.chieppa.arc.ui.tree.FileTreeBuilder
import dev.chieppa.arc.ui.tree.FileTreeItem
import dev.chieppa.arc.ui.tree.ReviewTree
import dev.chieppa.arc.ui.tree.SourceRows
import dev.chieppa.arc.ui.tree.TreeKeys
import dev.chieppa.arc.ui.tree.TreeOrder
import dev.chieppa.arc.ui.util.SourceLabels
import dev.chieppa.arc.ui.workbench.ReviewTargets
import dev.chieppa.arc.core.source.RepoName
import kotlinx.coroutines.test.runTest
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ReviewLogicTest {
    private val data = FakeReviewData()
    private val other = SourceRefs.parse("demo@${"c".repeat(40)}")!!

    private suspend fun loaded(): WorkspaceState {
        val state = WorkspaceState(repos = mapOf(data.repo to Path.of(".")))
        for (source in listOf(data.source, other)) {
            state.workspace = state.workspace.add(source)
            state.loads = state.loads + (source to SourceLoad.Ready(data.read(source)))
        }
        return state
    }

    private fun activate(state: WorkspaceState, source: dev.chieppa.arc.core.source.Source) {
        val (review, views) = SourceSwitching.activate(state.workspace, state.views, source)
        state.workspace = review
        state.views = views
    }

    @Test
    fun eachSourceKeepsItsOwnPanesAndPinnedPanesFollowEverySource() = runTest {
        val state = loaded()
        val panes = PaneController(state)
        panes.openChange(data.source, "app/Main.kt")
        val pinned = panes.openFile(data.repo, Revision(data.head.value), "docs/Guide.md", pinned = true).value
        activate(state, other)
        assertEquals(listOf(pinned), state.paneLayout.tabs)
        panes.openChange(other, "app/util/Text.kt")
        activate(state, data.source)
        assertEquals(2, state.paneLayout.tabs.size)
        assertTrue(pinned in state.paneLayout.tabs)
        panes.togglePin(pinned)
        activate(state, other)
        assertTrue(pinned !in state.paneLayout.tabs)
    }

    @Test
    fun openingAChangeTakesOverTheFocusedDiffUnlessAskedForANewTab() = runTest {
        val state = loaded()
        val panes = PaneController(state)
        panes.openChange(data.source, "app/Main.kt")
        panes.openChange(data.source, "app/util/Text.kt")
        assertEquals(1, state.paneLayout.tabs.size)
        panes.openChange(data.source, "docs/Guide.md", newTab = true)
        assertEquals(2, state.paneLayout.tabs.size)
        panes.close(panes.focused!!.id.value)
        assertEquals(1, state.workspace.panes.size)
    }

    @Test
    fun markAndAdvanceSkipsReviewedFilesAndWraps() = runTest {
        val state = loaded()
        val panes = PaneController(state)
        val marks = MarkController(state, panes)
        marks.setReviewed(data.source, listOf("docs/Guide.md"), true)
        panes.openChange(data.source, "app/Main.kt")
        marks.markAndAdvance()
        assertEquals("app/util/Text.kt", panes.focused!!.path)
        marks.markAndAdvance()
        assertEquals(setOf(FileReviewState.Reviewed), state.states(data.source).values.toSet())
        assertEquals(3, state.progress(data.source)!!.reviewed)
    }

    @Test
    fun theTreeJoinsLoneFoldersAndFoldsSetAsideFiles() {
        val files = listOf(
            ChangedFile("a/b/c/One.kt", ChangeStatus.Modified),
            ChangedFile("a/b/c/Two.kt", ChangeStatus.Modified),
            ChangedFile("z.lock", ChangeStatus.Modified),
            ChangedFile("Top.kt", ChangeStatus.Added),
        )
        val states = mapOf("a/b/c/One.kt" to FileReviewState.Reviewed, "z.lock" to FileReviewState.SetAside)
        val roots = FileTreeBuilder.build(listOf(SourceRows(data.source, "t", files, states, null)))
        val children = roots.single().children.map { it.item }
        val folder = children[0] as FileTreeItem.FolderRow
        assertEquals("a/b/c", folder.label)
        assertNull(folder.marked)
        assertEquals("Top.kt", (children[1] as FileTreeItem.FileRow).file.path)
        assertEquals(1, (children[2] as FileTreeItem.AsideRow).files)
        assertEquals(listOf("a/b/c/One.kt", "a/b/c/Two.kt", "Top.kt"), TreeOrder.files(roots, data.source).map { it.file.path })
    }

    @Test
    fun aSelectedFolderStandsForEveryFileUnderIt() = runTest {
        val state = loaded()
        val roots = ReviewTree.roots(state)
        val selected = ReviewTargets.selected(roots, setOf(TreeKeys.folder(data.source, "app")))
        assertEquals(listOf("app/util/Text.kt", "app/Main.kt"), selected[data.source])
    }

    @Test
    fun findHonoursCaseWholeWordAndRegex() {
        val lines = listOf("Token token tokens", "x")
        assertEquals(3, FindMatcher.find(lines, FindQuery("token"))!!.size)
        assertEquals(2, FindMatcher.find(lines, FindQuery("token", matchCase = true))!!.size)
        assertEquals(2, FindMatcher.find(lines, FindQuery("token", wholeWord = true))!!.size)
        assertEquals(1, FindMatcher.find(lines, FindQuery("^x$", regex = true))!!.size)
        assertNull(FindMatcher.find(lines, FindQuery("(", regex = true)))
    }

    @Test
    fun reposAreNamedAfterTheirFolderAndKeptUnique() {
        assertEquals(RepoName("my-repo"), RepoNames.fromFolder("my repo"))
        assertEquals(RepoName("api-2"), RepoNames.unique(RepoName("api"), setOf(RepoName("api"))))
    }

    @Test
    fun refsOnScreenUseShortShas() {
        assertEquals("demo@aaaaaaa..bbbbbbb", SourceLabels.of(data.source))
    }
}

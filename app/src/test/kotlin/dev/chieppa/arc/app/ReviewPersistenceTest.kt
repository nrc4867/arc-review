package dev.chieppa.arc.app

import dev.chieppa.arc.core.change.ObjectId
import dev.chieppa.arc.core.pane.Pane
import dev.chieppa.arc.core.pane.PaneId
import dev.chieppa.arc.core.pane.Revision
import dev.chieppa.arc.core.review.Review
import dev.chieppa.arc.core.review.ReviewId
import dev.chieppa.arc.core.source.RepoName
import dev.chieppa.arc.core.source.SourceRefs
import dev.chieppa.arc.ui.review.PaneUiState
import dev.chieppa.arc.ui.review.ReviewState
import dev.chieppa.arc.ui.review.SourceView
import dev.chieppa.arc.ui.tree.TreeKeys
import dev.chieppa.stretch.frame.PaneEdge
import dev.chieppa.stretch.frame.PaneLayout
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals

class ReviewPersistenceTest {
    private val source = SourceRefs.parse("api@1234abcd")!!
    private val other = SourceRefs.parse("web:feature...main")!!
    private val repos = mapOf(RepoName("api") to Path.of("/code/api"))

    private fun pane(id: String) = Pane(PaneId(id), RepoName("api"), "src/$id.kt", Revision("main"))

    @Test
    fun eachSourcesViewAndEachPanesStateComeBack() {
        val review = Review(ReviewId("r1"), "Auth").add(source).add(other).withPane(pane("p1")).withPane(pane("p2"))
        val layout = PaneLayout().open("p1").open("p2").splitWith("p2", PaneLayout.FIRST_LEAF, PaneEdge.End)
        val state = ReviewState(review, repos, mapOf(source to SourceView(layout, emptySet(), ObjectId("c".repeat(40)))), mapOf("p1" to PaneUiState(split = false)))
        state.paneLines["p2"] = 42
        val expanded = setOf<Any>(TreeKeys.source(source), TreeKeys.folder(source, "src"), TreeKeys.folder(other, "lib"))
        val back = ReviewPersistence.toState(ReviewPersistence.toStored(state, expanded), repos)
        assertEquals(review, back.review)
        assertEquals(layout, back.viewOf(source).layout)
        assertEquals(setOf(TreeKeys.source(source), TreeKeys.folder(source, "src")), back.viewOf(source).expanded)
        assertEquals(setOf(TreeKeys.folder(other, "lib")), back.viewOf(other).expanded)
        assertEquals(ObjectId("c".repeat(40)), back.viewOf(source).commit)
        assertEquals(false, back.paneState("p1").split)
        assertEquals(42, back.paneLines["p2"])
    }

    @Test
    fun aStoredLayoutLosesTabsWhosePanesAreGone() {
        val layout = PaneLayout().open("p1").open("gone")
        val stored = LayoutMapping.toStored(layout)
        assertEquals(listOf("p1"), LayoutMapping.toLayout(stored, setOf("p1"))!!.tabs)
    }

    @Test
    fun aNewReviewTakesAFreeId() {
        assertEquals("review-2", ReviewPersistence.newReview("Review", setOf("review-1")).id.value)
    }
}

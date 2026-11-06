package dev.chieppa.arc.store.review

import dev.chieppa.arc.core.change.ObjectId
import dev.chieppa.arc.core.mark.FileMark
import dev.chieppa.arc.core.mark.MarkKind
import dev.chieppa.arc.core.mark.PatchFingerprint
import dev.chieppa.arc.core.mark.PatchHash
import dev.chieppa.arc.core.mark.ReviewedRevisions
import dev.chieppa.arc.core.pane.Pane
import dev.chieppa.arc.core.pane.PaneId
import dev.chieppa.arc.core.pane.PaneView
import dev.chieppa.arc.core.pane.Revision
import dev.chieppa.arc.core.review.Review
import dev.chieppa.arc.core.review.ReviewId
import dev.chieppa.arc.core.source.RepoName
import dev.chieppa.arc.core.source.SourceRefs
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ReviewStoreTest {
    private val dir = Files.createTempDirectory("arc-reviews")
    private val range = SourceRefs.parse("stretch@1234abcd..5678abcd")!!
    private val branch = SourceRefs.parse("squash:main...dev")!!
    private val review = Review(ReviewId("r1"), "Auth rollout")
        .add(range).add(branch).activate(branch)
        .withPane(Pane(PaneId("p1"), RepoName("stretch"), "a/B.kt", Revision("5678abcd"), PaneView.FullFile, base = Revision("1234abcd"), source = range))
        .withPane(Pane(PaneId("p2"), RepoName("squash"), "README.md", Revision("main"), PaneView.Rendered, pinned = true))
        .mark(
            FileMark(
                range, "a/B.kt", PatchFingerprint(PatchHash("m"), PatchHash("e"), listOf(PatchHash("h1"))), MarkKind.Reviewed,
                ReviewedRevisions(ObjectId("a".repeat(40)), ObjectId("b".repeat(40))),
            ),
        )

    @Test
    fun aReviewWithItsViewsComesBackAsItWasSaved() {
        val layout = StoredPaneLayout(StoredPaneNode.Split("s1", "Row", listOf(StoredPaneNode.Leaf("a", listOf("p1")), StoredPaneNode.Leaf("b", listOf("p2"), "p2")), listOf(0.6f, 0.4f)), "a", 3)
        val views = listOf(StoredSourceView(range.ref, layout, listOf("stretch@1234abcd..5678abcd/a"), commit = "c".repeat(40)))
        val store = ReviewStore(dir)
        store.save(ReviewMapping.toStored(review, views, mapOf("p1" to StoredPaneState(split = false, line = 40))))
        val loaded = store.load("r1")!!
        assertEquals(review, ReviewMapping.toReview(loaded))
        assertEquals(views, loaded.views)
        assertEquals(StoredPaneState(false, 40), loaded.paneStates["p1"])
        assertEquals(listOf("Auth rollout"), store.all().map { it.name })
    }

    @Test
    fun entriesThatDoNotParseAreLeftOut() {
        val stored = ReviewMapping.toStored(review).copy(
            sources = listOf("not a ref", range.ref),
            active = "gone#1",
            panes = listOf(StoredPane("p", "bad repo name!", "x", "main", "Diff")),
            marks = listOf(StoredMark("x", "y", "Nonsense", "m", "e")),
        )
        val back = ReviewMapping.toReview(stored)
        assertEquals(listOf(range), back.sources)
        assertEquals(range, back.active)
        assertEquals(emptyList(), back.panes)
        assertEquals(emptyList(), back.marks)
    }

    @Test
    fun aMissingOrBrokenFileReadsAsNothing() {
        val store = ReviewStore(dir)
        assertNull(store.load("none"))
        Files.writeString(dir.resolve("broken.json"), "{ not json")
        assertNull(store.load("broken"))
        assertEquals(emptyList(), store.all())
    }
}

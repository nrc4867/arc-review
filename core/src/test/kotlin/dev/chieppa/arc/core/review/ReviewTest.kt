package dev.chieppa.arc.core.review

import dev.chieppa.arc.core.mark.FileMark
import dev.chieppa.arc.core.mark.MarkKind
import dev.chieppa.arc.core.mark.PatchFingerprint
import dev.chieppa.arc.core.mark.PatchHash
import dev.chieppa.arc.core.source.SourceRefs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class ReviewTest {
    private val prA = SourceRefs.parse("api#412")!!
    private val prB = SourceRefs.parse("web#88")!!
    private val commit = SourceRefs.parse("web@3fa9c1e")!!
    private val empty = Review(ReviewId("r1"), "Token rotation")

    @Test
    fun theFirstSourceAddedBecomesActive() {
        val review = empty.add(prA).add(prB)
        assertEquals(listOf(prA, prB), review.sources)
        assertEquals(prA, review.active)
    }

    @Test
    fun addingASourceTwiceKeepsOne() {
        assertEquals(listOf(prA), empty.add(prA).add(prA).sources)
    }

    @Test
    fun activatingOnlyTakesTheReviewsOwnSources() {
        val review = empty.add(prA).add(prB)
        assertEquals(prB, review.activate(prB).active)
        assertEquals(prA, review.activate(commit).active)
    }

    @Test
    fun removingTheActiveSourceActivatesTheNextOrElseThePrevious() {
        val review = empty.add(prA).add(prB).add(commit).activate(prB)
        assertEquals(commit, review.remove(prB).active)
        assertEquals(prB, review.activate(commit).remove(commit).active)
        assertEquals(prB, review.remove(prA).active)
        assertNull(empty.add(prA).remove(prA).active)
    }

    @Test
    fun aFileHoldsOneMarkInEachSource() {
        val fingerprint = PatchFingerprint(PatchHash("m"), PatchHash("a1"))
        val reviewed = FileMark(prA, "Token.kt", fingerprint, MarkKind.Reviewed)
        val aside = reviewed.copy(kind = MarkKind.SetAside)
        val review = empty.mark(reviewed).mark(aside)
        assertEquals(listOf(aside), review.marks)
        assertEquals(aside, review.markOf(prA, "Token.kt"))
        assertNull(review.markOf(prB, "Token.kt"))
        assertNull(review.unmark(prA, "Token.kt").markOf(prA, "Token.kt"))
        assertEquals(2, review.mark(reviewed.copy(source = prB)).marks.size)
    }

    @Test
    fun theActiveSourceMustBeInTheReview() {
        assertFailsWith<IllegalArgumentException> { Review(ReviewId("r"), "x", listOf(prA), active = prB) }
    }
}

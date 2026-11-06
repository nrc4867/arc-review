package dev.chieppa.arc.core.mark

import dev.chieppa.arc.core.change.ObjectId
import dev.chieppa.arc.core.source.SourceRefs
import kotlin.test.Test
import kotlin.test.assertEquals

class MarkRulesTest {
    private val source = SourceRefs.parse("api#412")!!
    private val path = "src/Token.kt"
    private val reviewed = ReviewedRevisions(ObjectId("a".repeat(40)), ObjectId("b".repeat(40)))
    private val fingerprint = PatchFingerprint(PatchHash("meta"), PatchHash("exact"))
    private val mark = FileMark(source, path, fingerprint, MarkKind.Reviewed, reviewed)
    private val other = fingerprint.copy(exact = PatchHash("other"))

    private fun replay(conflicted: Set<String> = emptySet(), differs: Set<String> = emptySet()) = ReplayOutcome(reviewed, conflicted, differs)

    @Test
    fun aFileThatLeftTheChangeClears() {
        assertEquals(MarkVerdict.Clear, MarkRules.verdict(mark, null, replay()))
    }

    @Test
    fun aChangedModeOrRenameClearsWhateverTheReplaySays() {
        assertEquals(MarkVerdict.Clear, MarkRules.verdict(mark, fingerprint.copy(meta = PatchHash("+x")), replay()))
    }

    @Test
    fun withoutAReplayTheExactFingerprintDecides() {
        assertEquals(MarkVerdict.Keep, MarkRules.verdict(mark, fingerprint))
        assertEquals(MarkVerdict.Clear, MarkRules.verdict(mark, other))
    }

    @Test
    fun aCleanReplayDecidesOverTheHash() {
        assertEquals(MarkVerdict.Keep, MarkRules.verdict(mark, other, replay()))
        assertEquals(MarkVerdict.Clear, MarkRules.verdict(mark, fingerprint, replay(differs = setOf(path))))
    }

    @Test
    fun aConflictedReplayFallsBackToTheHash() {
        assertEquals(MarkVerdict.Keep, MarkRules.verdict(mark, fingerprint, replay(conflicted = setOf(path), differs = setOf(path))))
        assertEquals(MarkVerdict.Clear, MarkRules.verdict(mark, other, replay(conflicted = setOf(path))))
    }

    @Test
    fun aReplayOfAnotherReviewIsNotUsed() {
        val elsewhere = ReplayOutcome(ReviewedRevisions(ObjectId("c".repeat(40)), ObjectId("d".repeat(40))), emptySet(), emptySet())
        assertEquals(MarkVerdict.Clear, MarkRules.verdict(mark, other, elsewhere))
    }
}

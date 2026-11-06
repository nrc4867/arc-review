package dev.chieppa.arc.core.mark

import dev.chieppa.arc.core.source.SourceRefs
import kotlin.test.Test
import kotlin.test.assertEquals

class FileReviewStateTest {
    private val source = SourceRefs.parse("api#412")!!
    private val first = PatchFingerprint(PatchHash("m"), PatchHash("a1"))
    private val second = PatchFingerprint(PatchHash("m"), PatchHash("b2"))

    private fun mark(kind: MarkKind, fingerprint: PatchFingerprint = first) = FileMark(source, "src/Token.kt", fingerprint, kind)

    @Test
    fun aFileWithNoMarkIsNotReviewed() {
        assertEquals(FileReviewState.NotReviewed, FileReviewState.of(null, first))
    }

    @Test
    fun aReviewedMarkHoldsWhileTheChangeIsTheSame() {
        assertEquals(FileReviewState.Reviewed, FileReviewState.of(mark(MarkKind.Reviewed), first))
    }

    @Test
    fun aReviewedMarkOnAnEarlierChangeIsChangedSince() {
        assertEquals(FileReviewState.ChangedSinceReviewed, FileReviewState.of(mark(MarkKind.Reviewed), second))
    }

    @Test
    fun setAsideHoldsWhateverTheChange() {
        assertEquals(FileReviewState.SetAside, FileReviewState.of(mark(MarkKind.SetAside), first))
        assertEquals(FileReviewState.SetAside, FileReviewState.of(mark(MarkKind.SetAside), second))
    }
}

package dev.chieppa.arc.core.mark

import dev.chieppa.arc.core.source.RepoName
import kotlin.test.Test
import kotlin.test.assertEquals

class FileReviewStateTest {
    private val api = RepoName("api")
    private val first = PatchHash("a1")
    private val second = PatchHash("b2")

    private fun mark(kind: MarkKind, patch: PatchHash = first) = FileMark(api, "src/Token.kt", patch, kind)

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

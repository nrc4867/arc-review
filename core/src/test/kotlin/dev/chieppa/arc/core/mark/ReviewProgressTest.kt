package dev.chieppa.arc.core.mark

import dev.chieppa.arc.core.mark.FileReviewState.ChangedSinceReviewed
import dev.chieppa.arc.core.mark.FileReviewState.NotReviewed
import dev.chieppa.arc.core.mark.FileReviewState.Reviewed
import dev.chieppa.arc.core.mark.FileReviewState.SetAside
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ReviewProgressTest {
    @Test
    fun setAsideFilesLeaveTheCountAndChangedFilesAreNotReviewed() {
        val progress = ReviewProgress.of(listOf(Reviewed, Reviewed, ChangedSinceReviewed, NotReviewed, SetAside, SetAside))
        assertEquals(ReviewProgress(2, 4), progress)
        assertFalse(progress.complete)
    }

    @Test
    fun everyCountedFileReviewedIsComplete() {
        assertTrue(ReviewProgress.of(listOf(Reviewed, SetAside)).complete)
        assertTrue(ReviewProgress.of(emptyList()).complete)
    }
}

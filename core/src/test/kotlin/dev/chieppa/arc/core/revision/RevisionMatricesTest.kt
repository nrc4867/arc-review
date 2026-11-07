package dev.chieppa.arc.core.revision

import dev.chieppa.arc.core.change.ObjectId
import dev.chieppa.arc.core.conversation.LineSide
import dev.chieppa.arc.core.conversation.ReviewThread
import dev.chieppa.arc.core.conversation.ThreadState
import dev.chieppa.arc.core.mark.FileMark
import dev.chieppa.arc.core.mark.FileReviewState
import dev.chieppa.arc.core.mark.MarkKind
import dev.chieppa.arc.core.mark.PatchFingerprint
import dev.chieppa.arc.core.mark.PatchHash
import dev.chieppa.arc.core.mark.ReviewedRevisions
import dev.chieppa.arc.core.source.SourceRefs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RevisionMatricesTest {
    private val source = SourceRefs.parse("api#412")!!
    private val base = id('b')

    private fun id(c: Char) = ObjectId(c.toString().repeat(ObjectId.SHA1_LENGTH))

    private fun print(exact: String) = PatchFingerprint(PatchHash("meta"), PatchHash(exact))

    private fun revision(n: Int, head: Char, vararg files: Pair<String, String>) =
        SourceRevision(n, id(head), base, n.toLong(), fingerprints = files.associate { (path, exact) -> path to print(exact) })

    // a.kt changes in r1 and r3, b.kt only in r2, c.kt in r1 and is dropped in r3.
    private val history = RevisionHistory(
        source,
        listOf(
            revision(1, '1', "src/a.kt" to "a1", "c.kt" to "c1"),
            revision(2, '2', "src/a.kt" to "a1", "src/b.kt" to "b1", "c.kt" to "c1"),
            revision(3, '3', "src/a.kt" to "a3", "src/b.kt" to "b1"),
        ),
    )

    private fun kinds(matrix: RevisionMatrix, path: String) = matrix.row(path)!!.cells.map { it.kind }

    @Test
    fun aFileChangesWhereItsPatchDiffersFromTheRevisionBefore() {
        val matrix = RevisionMatrices.of(history)
        assertEquals(listOf("src/a.kt", "src/b.kt", "c.kt"), matrix.rows.map { it.path })
        assertEquals(listOf(CellKind.Changed, CellKind.Unchanged, CellKind.Changed), kinds(matrix, "src/a.kt"))
        assertEquals(listOf(CellKind.Unchanged, CellKind.Changed, CellKind.Unchanged), kinds(matrix, "src/b.kt"))
        assertEquals(listOf(CellKind.Changed, CellKind.Unchanged, CellKind.Changed), kinds(matrix, "c.kt"))
    }

    @Test
    fun aMarkCoversChangesUpToItsRevisionAndFlagsLaterOnes() {
        val mark = FileMark(source, "src/a.kt", print("a1"), MarkKind.Reviewed, ReviewedRevisions(id('2'), base))
        val matrix = RevisionMatrices.of(history, mapOf("src/a.kt" to mark), mapOf("src/a.kt" to FileReviewState.ChangedSinceReviewed))
        assertEquals(listOf(CellKind.Reviewed, CellKind.Unchanged, CellKind.ChangedAfterReview), kinds(matrix, "src/a.kt"))
        val row = matrix.row("src/a.kt")!!
        assertEquals(1, row.markedAt)
        assertEquals(1, row.diffFrom(2))
        assertEquals(true, row.changedAfterReview)
    }

    @Test
    fun aMarkThatStillHoldsCoversEveryChange() {
        val mark = FileMark(source, "src/a.kt", print("a3"), MarkKind.Reviewed, ReviewedRevisions(id('1'), base))
        val matrix = RevisionMatrices.of(history, mapOf("src/a.kt" to mark), mapOf("src/a.kt" to FileReviewState.Reviewed))
        assertEquals(listOf(CellKind.Reviewed, CellKind.Unchanged, CellKind.Reviewed), kinds(matrix, "src/a.kt"))
    }

    @Test
    fun aMarkWhoseHeadIsUnknownIsPlacedByItsFingerprint() {
        val mark = FileMark(source, "src/a.kt", print("a1"), MarkKind.Reviewed, ReviewedRevisions(id('9'), base))
        val matrix = RevisionMatrices.of(history, mapOf("src/a.kt" to mark), mapOf("src/a.kt" to FileReviewState.ChangedSinceReviewed))
        assertEquals(1, matrix.row("src/a.kt")!!.markedAt)
    }

    @Test
    fun withoutAMarkTheDiffStartsFromTheLastChange() {
        val row = RevisionMatrices.of(history).row("src/a.kt")!!
        assertEquals(0, row.diffFrom(2))
        assertNull(row.diffFrom(0))
    }

    @Test
    fun threadsCountInTheRevisionTheyWereMadeOn() {
        fun thread(id: String, state: ThreadState, on: Char?) =
            ReviewThread(id, source, "src/b.kt", 1, LineSide.New, state, emptyList(), madeOn = on?.let(::id))
        val threads = listOf(
            thread("1", ThreadState.Open, '2'),
            thread("2", ThreadState.Addressed, '2'),
            thread("3", ThreadState.Resolved, '3'),
            thread("4", ThreadState.Draft, '3'),
            thread("5", ThreadState.Open, null),
        )
        val matrix = RevisionMatrices.of(history, threads = threads)
        val row = matrix.row("src/b.kt")!!
        assertEquals(ThreadCounts(open = 1, addressed = 1), row.cells[1].threads)
        assertEquals(ThreadCounts(resolved = 1), row.cells[2].threads)
        assertEquals(1, matrix.openThreads)
        assertEquals(true, row.hasThreads)
    }

    @Test
    fun theHistoryNamesTheRevisionThatBroughtACommit() {
        assertEquals(2, history.revisionOf(id('2'))?.number)
        assertNull(history.revisionOf(id('7')))
    }

    @Test
    fun patchesCompareFileByFile() {
        val differing = PatchChanges.differing(history.revisions[0].fingerprints, history.revisions[2].fingerprints)
        assertEquals(setOf("src/a.kt", "src/b.kt", "c.kt"), differing)
    }
}

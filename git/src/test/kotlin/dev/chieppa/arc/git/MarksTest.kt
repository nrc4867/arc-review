package dev.chieppa.arc.git

import dev.chieppa.arc.core.change.SourceRevisions
import dev.chieppa.arc.core.mark.FileMark
import dev.chieppa.arc.core.mark.FileReviewState
import dev.chieppa.arc.core.mark.MarkKind
import dev.chieppa.arc.core.mark.ReviewedRevisions
import dev.chieppa.arc.core.source.BranchName
import dev.chieppa.arc.core.source.RepoName
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.git.marks.MarkReplayer
import dev.chieppa.arc.git.repo.GitRepository
import dev.chieppa.arc.git.source.SourceReader
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Rebase, amend and conflict cases from docs/research/review-marks.md, on 40-line files: a mark taken at one head is
 * kept or cleared at the next as the research says it should be.
 */
class MarksTest {
    private val source = Source.BranchAgainstBase(RepoName("demo"), BranchName("feature"), BranchName("main"))
    private val path = "File.kt"
    private val repo = TestRepo()

    init {
        repo.commit("Base", mapOf(path to lines(40), "Other.kt" to "x\n"))
        repo.git("checkout", "-q", "-b", "feature")
        repo.commit("Edit line 20", mapOf(path to lines(40) { i, l -> if (i == 20) "line 20 changed" else l }))
    }

    private val git = GitRepository(repo.dir)
    private val reader = SourceReader(git)

    private suspend fun markNow(): FileMark {
        val snapshot = reader.read(source)
        return FileMark(source, path, snapshot.fingerprints.getValue(path), MarkKind.Reviewed, ReviewedRevisions(snapshot.revisions.head, snapshot.revisions.base))
    }

    private suspend fun stateAfter(mark: FileMark): FileReviewState {
        val snapshot = reader.read(source)
        val replay = MarkReplayer(git).replay(mark.reviewed!!, snapshot.revisions)
        return FileReviewState.of(mark, snapshot.fingerprints[path], replay)
    }

    private fun onMain(message: String, files: Map<String, String>) {
        repo.git("checkout", "-q", "main")
        repo.commit(message, files)
        repo.git("checkout", "-q", "feature")
    }

    @Test
    fun aRebaseOverAnotherFileKeepsTheMark() = runTest {
        val mark = markNow()
        onMain("Other", mapOf("Other.kt" to "y\n"))
        repo.git("rebase", "-q", "main")
        assertEquals(FileReviewState.Reviewed, stateAfter(mark))
    }

    @Test
    fun aRebaseOverEditsInTheContextKeepsTheMark() = runTest {
        val mark = markNow()
        onMain("Near", mapOf(path to lines(40) { i, l -> if (i == 18) "line 18 upstream" else l }))
        repo.git("rebase", "-q", "main")
        assertEquals(FileReviewState.Reviewed, stateAfter(mark))
    }

    @Test
    fun aNewHunkInTheReviewedFileClearsTheMark() = runTest {
        val mark = markNow()
        repo.commit("More", mapOf(path to lines(40) { i, l -> if (i == 20) "line 20 changed" else if (i == 35) "line 35 too" else l }))
        assertEquals(FileReviewState.ChangedSinceReviewed, stateAfter(mark))
    }

    @Test
    fun anAmendedChangeClearsTheMark() = runTest {
        val mark = markNow()
        repo.write(path, lines(40) { i, l -> if (i == 20) "line 20 changed again" else l })
        repo.git("commit", "-q", "-a", "--amend", "-m", "Edit line 20")
        assertEquals(FileReviewState.ChangedSinceReviewed, stateAfter(mark))
    }

    @Test
    fun aConflictTheAuthorResolvedClearsTheMark() = runTest {
        val mark = markNow()
        onMain("Same line", mapOf(path to lines(40) { i, l -> if (i == 20) "line 20 upstream" else l }))
        repo.git("reset", "-q", "--hard", "main")
        repo.commit("Edit line 20 again", mapOf(path to lines(40) { i, l -> if (i == 20) "line 20 resolved" else l }))
        val snapshot = reader.read(source)
        val replay = MarkReplayer(git).replay(mark.reviewed!!, snapshot.revisions)!!
        assertEquals(setOf(path), replay.conflicted)
        assertEquals(FileReviewState.ChangedSinceReviewed, FileReviewState.of(mark, snapshot.fingerprints[path], replay))
    }

    @Test
    fun aRebaseWhereTheBaseTookOneLineOfTheChangeKeepsTheMark() = runTest {
        repo.commit("Second edit", mapOf(path to lines(40) { i, l -> if (i == 20) "line 20 changed" else if (i == 5) "line 5 changed" else l }))
        val mark = markNow()
        onMain("Upstream took line 5", mapOf(path to lines(40) { i, l -> if (i == 5) "line 5 changed" else l }))
        repo.git("rebase", "-q", "main")
        val snapshot = reader.read(source)
        val replay = MarkReplayer(git).replay(mark.reviewed!!, snapshot.revisions)
        assertEquals(FileReviewState.Reviewed, FileReviewState.of(mark, snapshot.fingerprints[path], replay))
        assertEquals(FileReviewState.ChangedSinceReviewed, FileReviewState.of(mark, snapshot.fingerprints[path]))
    }

    @Test
    fun anUnmovedSourceNeedsNoMerge() = runTest {
        val mark = markNow()
        val snapshot = reader.read(source)
        assertEquals(SourceRevisions(mark.reviewed!!.mergeBase, mark.reviewed!!.head), snapshot.revisions)
        assertEquals(FileReviewState.Reviewed, stateAfter(mark))
    }
}

package dev.chieppa.arc.git

import dev.chieppa.arc.core.change.ChangeStatus
import dev.chieppa.arc.core.source.BranchName
import dev.chieppa.arc.core.source.CommitSha
import dev.chieppa.arc.core.source.RepoName
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.git.process.GitSettings
import dev.chieppa.arc.git.repo.GitRepository
import dev.chieppa.arc.git.source.SourceNotFoundException
import dev.chieppa.arc.git.source.SourceReader
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SourceReaderTest {
    private val name = RepoName("demo")
    private val repo = TestRepo()
    private val first = repo.commit("First", mapOf("app/Main.kt" to lines(10), "old.txt" to "old\n", "gone.txt" to "bye\n"))

    init {
        repo.writeBytes("logo.bin", byteArrayOf(0, 1, 2))
        repo.git("rm", "-q", "gone.txt")
        repo.git("mv", "old.txt", "new name.txt")
    }

    private val second = repo.commit("Second", mapOf("app/Main.kt" to lines(10) { i, l -> if (i == 3) "changed" else l } + "line 11\n", "app/Added.kt" to "a\nb\n"))

    private fun reader() = SourceReader(GitRepository(repo.dir))

    @Test
    fun aCommitIsComparedWithItsParent() = runTest {
        val snapshot = reader().read(Source.Commit(name, CommitSha(second.short())))
        assertEquals(first, snapshot.revisions.base)
        assertEquals(second, snapshot.revisions.head)
        assertEquals("Second", snapshot.title)
        assertEquals(listOf("Second"), snapshot.commits.map { it.subject })
        val byPath = snapshot.files.associateBy { it.path }
        assertEquals(listOf("app/Added.kt", "app/Main.kt", "gone.txt", "logo.bin", "new name.txt"), snapshot.files.map { it.path })
        assertEquals(ChangeStatus.Added, byPath.getValue("app/Added.kt").status)
        assertEquals(2 to 0, byPath.getValue("app/Added.kt").let { it.added to it.removed })
        assertEquals(2 to 1, byPath.getValue("app/Main.kt").let { it.added to it.removed })
        assertEquals(ChangeStatus.Renamed, byPath.getValue("new name.txt").status)
        assertEquals("old.txt", byPath.getValue("new name.txt").oldPath)
        assertEquals(ChangeStatus.Deleted, byPath.getValue("gone.txt").status)
        assertNull(byPath.getValue("gone.txt").newBlob)
        assertTrue(byPath.getValue("logo.bin").binary)
        assertEquals(snapshot.files.map { it.path }.toSet(), snapshot.fingerprints.keys)
    }

    @Test
    fun aRootCommitIsComparedWithTheEmptyTree() = runTest {
        val snapshot = reader().read(Source.Commit(name, CommitSha(first.value)))
        assertEquals(GitSettings.EMPTY_TREE_SHA1, snapshot.revisions.base.value)
        assertEquals(setOf("app/Main.kt", "old.txt", "gone.txt"), snapshot.files.map { it.path }.toSet())
    }

    @Test
    fun aRangeListsItsCommitsOldestFirst() = runTest {
        val third = repo.commit("Third", mapOf("app/Added.kt" to "a\nb\nc\n"))
        val snapshot = reader().read(Source.CommitRange(name, CommitSha(first.value), CommitSha(third.value)))
        assertEquals(listOf("Second", "Third"), snapshot.commits.map { it.subject })
        assertEquals("Third", snapshot.title)
        assertEquals(3, snapshot.file("app/Added.kt")!!.added)
    }

    @Test
    fun aBranchIsComparedWithItsMergeBaseWithTheDefaultBranch() = runTest {
        repo.git("checkout", "-q", "-b", "feature")
        val feature = repo.commit("Feature", mapOf("app/Feature.kt" to "f\n"))
        repo.git("checkout", "-q", "main")
        repo.commit("Main moves on", mapOf("app/Other.kt" to "o\n"))
        val snapshot = reader().read(Source.BranchAgainstBase(name, BranchName("feature")))
        assertEquals(second, snapshot.revisions.base)
        assertEquals(feature, snapshot.revisions.head)
        assertEquals(listOf("app/Feature.kt"), snapshot.files.map { it.path })
    }

    @Test
    fun oneCommitOfASourceReadsAlone() = runTest {
        val third = repo.commit("Third", mapOf("app/Added.kt" to "a\nb\nc\n"))
        val snapshot = reader().readCommit(Source.CommitRange(name, CommitSha(first.value), CommitSha(third.value)), second)
        assertEquals(listOf("Second"), snapshot.commits.map { it.subject })
        assertEquals(5, snapshot.files.size)
    }

    @Test
    fun aMissingCommitOrPullRequestCannotBeRead() = runTest {
        assertFailsWith<SourceNotFoundException> { reader().read(Source.Commit(name, CommitSha("deadbeef"))) }
        assertFailsWith<SourceNotFoundException> { reader().read(Source.PullRequest(name, 1)) }
    }
}

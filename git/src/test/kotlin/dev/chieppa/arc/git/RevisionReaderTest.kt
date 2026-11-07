package dev.chieppa.arc.git

import dev.chieppa.arc.core.change.ObjectId
import dev.chieppa.arc.core.change.SourceRevisions
import dev.chieppa.arc.core.revision.CellKind
import dev.chieppa.arc.core.revision.RecordedHead
import dev.chieppa.arc.core.revision.RevisionMatrices
import dev.chieppa.arc.core.source.BranchName
import dev.chieppa.arc.core.source.RepoName
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.git.repo.GitRepository
import dev.chieppa.arc.git.revision.PushHistoryLookup
import dev.chieppa.arc.git.revision.PushedHead
import dev.chieppa.arc.git.revision.RevisionReader
import dev.chieppa.arc.git.revision.RevisionRefNames
import dev.chieppa.arc.git.source.PullRequestLookup
import dev.chieppa.arc.git.source.PullRequestRefs
import dev.chieppa.arc.git.source.SourceReader
import dev.chieppa.arc.git.source.SourceResolver
import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * A pull request pushed four times: r1 adds a.kt and c.kt, r2 adds b.kt, r3 is the same change rebased onto a main
 * that moved, and r4 changes a.kt again. Only r4 is reachable on origin; r1 and r2 survive there unreferenced, as
 * GitHub keeps force-pushed heads.
 */
class RevisionReaderTest {
    private val origin = TestRepo()
    private val start = origin.commitAt(T0, "Start", mapOf("app/Main.kt" to lines(5)))
    private val r1: ObjectId
    private val r2: ObjectId
    private val r3: ObjectId
    private val r4: ObjectId
    private val moved: ObjectId

    init {
        origin.git("config", "uploadpack.allowAnySHA1InWant", "true")
        origin.git("checkout", "-q", "-b", "feature")
        r1 = origin.commitAt(T0 + 10, "Add a and c", mapOf("src/a.kt" to lines(3), "c.kt" to "c\n"))
        r2 = origin.commitAt(T0 + 20, "Add b", mapOf("src/b.kt" to "b\n"))
        origin.git("checkout", "-q", "main")
        moved = origin.commitAt(T0 + 30, "Main moves", mapOf("app/Main.kt" to lines(6)))
        origin.git("checkout", "-q", "feature")
        origin.gitAt(T0 + 40, "rebase", "-q", "main")
        r3 = origin.head()
        r4 = origin.commitAt(T0 + 50, "Change a", mapOf("src/a.kt" to lines(4)))
        origin.git("update-ref", "refs/pull/7/head", r4.value)
        origin.git("checkout", "-q", "main")
        origin.git("branch", "-q", "-D", "feature")
    }

    private val cloneDir = Files.createTempDirectory("arc-git-clone").resolve("clone")

    init {
        origin.git("clone", "-q", origin.dir.toString(), cloneDir.toString())
    }

    private val source = Source.PullRequest(RepoName("demo"), 7)
    private val repo = GitRepository(cloneDir)
    private val pulls = PullRequestLookup { _, _ -> PullRequestRefs("main", headOid = r4.value) }

    private fun reader(vararg pushed: PushedHead) =
        RevisionReader(repo, SourceResolver(repo, pulls), PushHistoryLookup { _, _ -> pushed.toList() })

    @Test
    fun headsFromTheHostAreFetchedPinnedAndOrdered() = runTest {
        val snapshot = SourceReader(repo, pulls).read(source)
        val recorded = listOf(RecordedHead(snapshot.revisions.head, snapshot.revisions.base, T0 + 100))
        val history = reader(PushedHead(r1.value), PushedHead(r2.value), PushedHead(r3.value, T0 + 45)).read(source, recorded)

        assertEquals(listOf(r1, r2, r3, r4), history.revisions.map { it.head })
        assertEquals(listOf(start, start, moved, moved), history.revisions.map { it.base })
        assertEquals(listOf(false, false, true, false), history.revisions.map { it.rewrote })
        assertEquals(listOf(false, false, true, false), history.revisions.map { it.rebased })
        assertEquals(T0 + 45, history.revisions[2].at)
        assertEquals(listOf(listOf("Add a and c"), listOf("Add b"), listOf("Add a and c", "Add b"), listOf("Change a")), history.revisions.map { r -> r.commits.map { it.subject } })
        for (head in listOf(r1, r2, r3, r4)) assertEquals(head, repo.resolveCommit(RevisionRefNames.pin(head)))
    }

    @Test
    fun aPureRebaseChangesNoFileInTheMatrix() = runTest {
        val snapshot = SourceReader(repo, pulls).read(source)
        val recorded = listOf(RecordedHead(snapshot.revisions.head, snapshot.revisions.base, T0 + 100))
        val history = reader(PushedHead(r1.value), PushedHead(r2.value), PushedHead(r3.value)).read(source, recorded)
        val matrix = RevisionMatrices.of(history)
        assertEquals(listOf("src/a.kt", "src/b.kt", "c.kt"), matrix.rows.map { it.path })
        val c = CellKind.Changed
        val u = CellKind.Unchanged
        assertEquals(listOf(c, u, u, c), matrix.row("src/a.kt")!!.cells.map { it.kind })
        assertEquals(listOf(u, c, u, u), matrix.row("src/b.kt")!!.cells.map { it.kind })
    }

    @Test
    fun aHeadNoOneCanFetchIsLeftOut() = runTest {
        val snapshot = SourceReader(repo, pulls).read(source)
        val recorded = listOf(RecordedHead(snapshot.revisions.head, snapshot.revisions.base, T0 + 100))
        val history = reader(PushedHead("1".repeat(40))).read(source, recorded)
        assertEquals(listOf(r4), history.revisions.map { it.head })
    }

    @Test
    fun patchesBetweenTwoCommitsAreComparedThroughTheRebase() = runTest {
        SourceReader(repo, pulls).read(source)
        reader(PushedHead(r2.value)).read(source, emptyList())
        assertEquals(setOf("src/a.kt"), RevisionReader(repo).differing(SourceRevisions(start, r2), SourceRevisions(moved, r4)))
    }

    @Test
    fun aBranchHasTheHeadsArcRecorded() = runTest {
        val local = TestRepo()
        val base = local.commitAt(T0, "Start", mapOf("x.kt" to "x\n"))
        local.git("checkout", "-q", "-b", "topic")
        val first = local.commitAt(T0 + 10, "One", mapOf("y.kt" to "y\n"))
        val second = local.commitAt(T0 + 20, "Two", mapOf("y.kt" to "y2\n"))
        val branch = Source.BranchAgainstBase(RepoName("local"), BranchName("topic"), BranchName("main"))
        val git = GitRepository(local.dir)
        val recorded = listOf(RecordedHead(second, base, T0 + 30), RecordedHead(first, base, T0 + 15))
        val history = RevisionReader(git).read(branch, recorded)
        assertEquals(listOf(first, second), history.revisions.map { it.head })
        assertEquals(listOf(1, 2), history.revisions.map { it.number })
        assertEquals(T0 + 10, history.revisions[0].at)
    }

    private companion object {
        const val T0 = 1_700_000_000L
    }
}

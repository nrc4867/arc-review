package dev.chieppa.arc.git

import dev.chieppa.arc.core.pane.Revision
import dev.chieppa.arc.core.source.RepoName
import dev.chieppa.arc.git.repo.GitRepositories
import dev.chieppa.arc.git.repo.GitRepository
import dev.chieppa.arc.git.repo.UnknownRepoException
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class GitRepositoryTest {
    private val repo = TestRepo()
    private val first = repo.commit("First", mapOf("a.txt" to "one\n", "dir/b.txt" to "two\n"))
    private val second = repo.commit("Second", mapOf("a.txt" to "uno\n"))

    @Test
    fun filesReadByRevisionAndPathThroughOneProcess() = runTest {
        GitRepository(repo.dir).use { git ->
            assertEquals("one\n", git.readFile(first.value, "a.txt")!!.decodeToString())
            assertEquals("uno\n", git.readFile("main", "a.txt")!!.decodeToString())
            assertNull(git.readFile("main", "missing.txt"))
            assertNull(git.readFile("main", "dir"))
            assertEquals("two\n", git.readFile("main", "dir/b.txt")!!.decodeToString())
        }
    }

    @Test
    fun manyReadersAtOnceEachGetTheirOwnAnswer() = runTest {
        GitRepository(repo.dir).use { git ->
            val reads = withContext(Dispatchers.Default) {
                (1..40).map { i -> async { git.readFile(if (i % 2 == 0) first.value else second.value, "a.txt")!!.decodeToString() } }.awaitAll()
            }
            reads.forEachIndexed { i, text -> assertEquals(if ((i + 1) % 2 == 0) "one\n" else "uno\n", text) }
        }
    }

    @Test
    fun aCancelledReadLeavesTheNextOneWhole() = runTest {
        GitRepository(repo.dir).use { git ->
            val job = launch(Dispatchers.IO, start = CoroutineStart.UNDISPATCHED) { git.readFile("main", "a.txt") }
            job.cancel()
            job.join()
            assertContentEquals("uno\n".toByteArray(), git.readFile("main", "a.txt"))
        }
    }

    @Test
    fun theWholeTreeIsListedWithFilesThatDidNotChange() = runTest {
        GitRepository(repo.dir).use { git ->
            assertEquals(listOf("a.txt", "dir/b.txt"), git.listFiles(second.value))
        }
    }

    @Test
    fun branchesListNewestFirstWithTheirLastAuthor() = runTest {
        repo.git("branch", "spike/token-binding", first.value)
        repo.git("checkout", "-q", "-b", "later")
        repo.commitAt(4_000_000_000, "Later")
        repo.git("checkout", "-q", "main")
        GitRepository(repo.dir).use { git ->
            val branches = git.branches()
            assertEquals("later", branches.first().name)
            assertEquals(4_000_000_000, branches.first().committed)
            assertEquals(setOf("later", "main", "spike/token-binding"), branches.map { it.name }.toSet())
            assertEquals("Test", branches.single { it.name == "spike/token-binding" }.author)
        }
    }

    @Test
    fun branchesCountWhatTheyHaveThatTheDefaultBranchDoesNot() = runTest {
        repo.git("branch", "merged", first.value)
        repo.git("checkout", "-q", "-b", "feature")
        repo.commit("One", mapOf("a.txt" to "a"))
        repo.commit("Two", mapOf("b.txt" to "b"))
        repo.git("checkout", "-q", "main")
        GitRepository(repo.dir).use { git ->
            val ahead = git.branches().associate { it.name to it.ahead }
            assertEquals(mapOf("feature" to 2, "main" to 0, "merged" to 0), ahead)
        }
    }

    @Test
    fun recentCommitsComeNewestFirstFromEveryBranch() = runTest {
        repo.git("checkout", "-q", "-b", "feature")
        val third = repo.commitAt(4_000_000_000, "Third", mapOf("c.txt" to "c"))
        repo.git("checkout", "-q", "main")
        GitRepository(repo.dir).use { git ->
            val commits = git.recentCommits(10)
            assertEquals(listOf(third, second, first), commits.map { it.id })
            assertEquals("Third", commits.first().subject)
            assertEquals(listOf(third), git.recentCommits(1).map { it.id })
        }
    }

    @Test
    fun revisionsResolveToCommitsOrNothing() = runTest {
        GitRepository(repo.dir).use { git ->
            assertEquals(second, git.resolveCommit("main"))
            assertEquals(first, git.resolveCommit("main~1"))
            assertEquals(first, git.firstParent(second))
            assertNull(git.firstParent(first))
            assertNull(git.resolveCommit("no-such-branch"))
            assertEquals("main", git.defaultBranch())
        }
    }

    @Test
    fun reposAreFoundThroughTheLocator() = runTest {
        val name = RepoName("demo")
        GitRepositories({ if (it == name) repo.dir else null }).use { repos ->
            assertEquals("two\n", repos.readFile(name, Revision("main"), "dir/b.txt")!!.decodeToString())
            assertFailsWith<UnknownRepoException> { repos.repository(RepoName("other")) }
        }
    }
}

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

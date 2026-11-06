package dev.chieppa.arc.git

import dev.chieppa.arc.core.source.CommitSha
import dev.chieppa.arc.core.source.RepoName
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.git.repo.GitRepository
import dev.chieppa.arc.git.source.SourceReader
import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** A blobless clone never fetches a blob on its own; reading a source fetches its trees' blobs in one batch. */
class PartialCloneTest {
    private val origin = TestRepo()
    private val first = origin.commit("First", mapOf("a.txt" to "one\n", "b.txt" to "bee\n"))
    private val second = origin.commit("Second", mapOf("a.txt" to "two\n"))
    private val clone: TestRepo

    init {
        origin.git("config", "uploadpack.allowFilter", "true")
        origin.git("config", "uploadpack.allowAnySHA1InWant", "true")
        val target = Files.createTempDirectory("arc-partial").resolve("clone")
        TestRepo(target.parent).git("clone", "-q", "--no-checkout", "--filter=blob:none", origin.dir.toUri().toString(), target.toString())
        clone = TestRepo(target)
    }

    @Test
    fun aMissingBlobReadsAsNothingRatherThanBeingFetched() = runTest {
        GitRepository(clone.dir).use { git ->
            assertTrue(git.isPartial())
            assertNull(git.readFile(second.value, "a.txt"))
        }
    }

    @Test
    fun readingASourceFetchesBothTreesFirst() = runTest {
        GitRepository(clone.dir).use { git ->
            val snapshot = SourceReader(git).read(Source.Commit(RepoName("demo"), CommitSha(second.value)))
            assertEquals(listOf("a.txt"), snapshot.files.map { it.path })
            assertEquals("two\n", git.readFile(second.value, "a.txt")!!.decodeToString())
            assertEquals("one\n", git.readFile(first.value, "a.txt")!!.decodeToString())
            assertEquals("bee\n", git.readFile(second.value, "b.txt")!!.decodeToString())
        }
    }
}

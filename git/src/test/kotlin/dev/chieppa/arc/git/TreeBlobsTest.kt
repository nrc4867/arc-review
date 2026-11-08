package dev.chieppa.arc.git

import dev.chieppa.arc.core.change.ObjectId
import dev.chieppa.arc.git.repo.GitRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TreeBlobsTest {
    @Test
    fun aTreeListsEveryFileWithItsBlobAndSize() = runTest {
        val repo = TestRepo()
        val head = repo.commit("one", mapOf("src/A.kt" to "class A\n", "README.md" to "hello\n"))
        GitRepository(repo.dir).use { git ->
            val blobs = git.listBlobs(head.value).associateBy { it.path }
            assertEquals(setOf("src/A.kt", "README.md"), blobs.keys)
            assertEquals(8L, blobs.getValue("src/A.kt").size)
            assertEquals(repo.rev("HEAD:src/A.kt"), blobs.getValue("src/A.kt").blob)
        }
    }

    @Test
    fun blobsReadInOneStreamInOrderWithNullForOneMissing() = runTest {
        val repo = TestRepo()
        repo.commit("one", mapOf("a.txt" to "a\n", "b.txt" to "bb\n"))
        val missing = ObjectId("0123456789012345678901234567890123456789")
        val read = ArrayList<Pair<ObjectId, String?>>()
        GitRepository(repo.dir).use { git ->
            git.readBlobs(listOf(repo.rev("HEAD:b.txt"), missing, repo.rev("HEAD:a.txt"))) { id, bytes -> read += id to bytes?.toString(Charsets.UTF_8) }
            assertEquals(listOf("bb\n", null, "a\n"), read.map { it.second })
            assertEquals("a\n", git.readBlob(repo.rev("HEAD:a.txt"))?.toString(Charsets.UTF_8))
            assertNull(git.readBlob(missing))
        }
    }
}

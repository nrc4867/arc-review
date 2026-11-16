package dev.chieppa.arc.index.facts

import dev.chieppa.arc.core.source.RepoName
import dev.chieppa.arc.index.FakeBlobStore
import dev.chieppa.arc.index.IndexStatus
import dev.chieppa.arc.index.IndexTarget
import dev.chieppa.arc.index.WorkspaceIndex
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.io.path.createTempDirectory
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class KeptFactsTest {
    private val folder = createTempDirectory("arc-kept-facts")

    @AfterTest
    fun cleanUp() {
        folder.toFile().deleteRecursively()
    }

    @Test
    fun aLaterIndexWithTheSameStoreParsesNothingAgain() = runTest {
        val blobs = FakeBlobStore().apply {
            put("api", "a1", mapOf("src/Token.kt" to "package auth\n\nclass Token(val raw: String)\n", "src/Use.kt" to "fun use() = Token(\"x\")\n"))
        }
        val targets = listOf(IndexTarget("api#1", RepoName("api"), "a1"))
        val dispatcher = StandardTestDispatcher(testScheduler)
        WorkspaceIndex(blobs, dispatcher, DiskFactsStore(folder, VERSION)).update(targets)
        val read = blobs.reads
        val later = WorkspaceIndex(blobs, dispatcher, DiskFactsStore(folder, VERSION))
        later.update(targets)
        assertEquals(read, blobs.reads)
        assertEquals(IndexStatus(sources = 1, files = 2), later.status.value)
    }

    private companion object {
        const val VERSION = "test"
    }
}

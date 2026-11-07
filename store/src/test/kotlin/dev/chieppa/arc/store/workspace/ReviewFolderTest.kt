package dev.chieppa.arc.store.workspace

import java.nio.file.Files
import java.nio.file.attribute.FileTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ReviewFolderTest {
    private val dir = Files.createTempDirectory("arc-reviews")

    private fun review(id: String, source: String, modified: Long) {
        val file = dir.resolve("$id.json")
        Files.writeString(file, """{ "id": "$id", "name": "Review $id", "sources": ["$source"], "active": "$source" }""")
        Files.setLastModifiedTime(file, FileTime.fromMillis(modified))
    }

    @Test
    fun theNamedReviewReadsAsAWorkspace() {
        review("review-1", "api#1", 2_000)
        review("review-2", "web#2", 1_000)
        val stored = ReviewFolder(dir).startFrom("review-2")!!
        assertEquals(listOf("web#2"), stored.sources)
        assertEquals("web#2", stored.active)
    }

    @Test
    fun withoutTheNamedReviewTheNewestReadsAndBrokenFilesAreSkipped() {
        review("review-1", "api#1", 1_000)
        review("review-2", "web#2", 2_000)
        Files.writeString(dir.resolve("review-3.json"), "{ not json")
        Files.setLastModifiedTime(dir.resolve("review-3.json"), FileTime.fromMillis(3_000))
        assertEquals(listOf("web#2"), ReviewFolder(dir).startFrom("gone")!!.sources)
        assertEquals(listOf("web#2"), ReviewFolder(dir).startFrom(null)!!.sources)
    }

    @Test
    fun aMissingFolderHoldsNothing() {
        assertNull(ReviewFolder(dir.resolve("none")).startFrom("review-1"))
    }
}

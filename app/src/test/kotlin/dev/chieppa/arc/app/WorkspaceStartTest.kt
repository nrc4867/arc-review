package dev.chieppa.arc.app

import dev.chieppa.arc.core.source.SourceRefs
import dev.chieppa.arc.host.conversation.NoConversation
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.FileTime
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WorkspaceStartTest {
    private val dir = Files.createTempDirectory("arc-data")
    private val reviews = Files.createDirectories(dir.resolve(AppConstants.REVIEWS_FOLDER))

    private fun review(id: String, modified: Long, text: String): Path {
        val file = reviews.resolve("$id.json")
        Files.writeString(file, text)
        Files.setLastModifiedTime(file, FileTime.fromMillis(modified))
        return file
    }

    private val authReview = """
        {
          "id": "review-1",
          "name": "Auth",
          "sources": ["api#412", "web@1234abcd"],
          "active": "web@1234abcd",
          "panes": [{ "id": "p1", "repo": "api", "path": "src/A.kt", "revision": "main", "view": "Diff", "source": "api#412" }],
          "marks": [{ "source": "api#412", "path": "src/A.kt", "kind": "Reviewed", "meta": "m", "exact": "e" }],
          "views": [{ "source": "api#412", "expanded": ["api#412/src"] }],
          "paneStates": { "p1": { "split": false, "line": 12 } },
          "pages": [{ "type": "filter", "id": "p2", "filter": "f1" }],
          "heads": [{ "source": "api#412", "heads": [{ "head": "${"c".repeat(40)}", "base": "${"d".repeat(40)}", "at": 1700000000 }] }]
        }
    """.trimIndent()

    private fun session() = AppSession(dir, NoConversation)

    @Test
    fun aFirstRunStartsFromTheLastOpenReviewAndLeavesTheReviewsFolderAlone() {
        val first = review("review-1", 1_000, authReview)
        val newer = review("review-2", 2_000, """{ "id": "review-2", "name": "Other", "sources": ["web#9"] }""")
        Files.writeString(dir.resolve(AppConstants.SETTINGS_FILE), "review.last=review-1\n")
        val before = first.readText()

        val state = session().use { it.open() }
        val api = SourceRefs.parse("api#412")!!
        assertEquals(listOf(api, SourceRefs.parse("web@1234abcd")!!), state.workspace.sources)
        assertEquals("web@1234abcd", state.active?.ref)
        assertEquals(listOf("p1"), state.workspace.panes.map { it.id.value })
        assertEquals(listOf("src/A.kt"), state.workspace.marksIn(api).keys.toList())
        assertEquals(1, state.workspace.headsOf(api).size)
        assertEquals(setOf("api#412/src"), state.viewOf(api).expanded)
        assertEquals(12, state.paneLines["p1"])

        assertTrue(dir.resolve(AppConstants.WORKSPACE_FILE).exists())
        assertEquals(before, first.readText())
        assertTrue(newer.exists())
    }

    @Test
    fun withoutALastOpenReviewTheNewestIsTaken() {
        review("review-1", 1_000, authReview)
        review("review-2", 2_000, """{ "id": "review-2", "name": "Other", "sources": ["web#9"] }""")
        assertEquals(listOf("web#9"), session().use { it.open() }.workspace.sources.map { it.ref })
    }

    @Test
    fun theWorkspaceFileWinsOverTheReviewsFolder() {
        review("review-1", 1_000, authReview)
        session().use { it.open() }
        Files.writeString(dir.resolve(AppConstants.WORKSPACE_FILE), """{ "sources": ["web#9"] }""")
        assertEquals(listOf("web#9"), session().use { it.open() }.workspace.sources.map { it.ref })
    }

    @Test
    fun withNothingSavedTheWorkspaceStartsEmpty() {
        val state = session().use { it.open() }
        assertEquals(emptyList(), state.workspace.sources)
        assertTrue(dir.resolve(AppConstants.WORKSPACE_FILE).exists())
    }
}

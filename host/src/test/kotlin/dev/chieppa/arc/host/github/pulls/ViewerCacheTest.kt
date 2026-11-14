package dev.chieppa.arc.host.github.pulls

import dev.chieppa.arc.host.github.FakeGitHub
import dev.chieppa.arc.host.github.GitHub
import dev.chieppa.arc.host.github.RecordedTransport
import dev.chieppa.arc.host.github.api.ApiResponse
import dev.chieppa.arc.host.github.api.GitHubException
import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ViewerCacheTest {
    private val folder: Path = Files.createTempDirectory("viewer")
    private val path: Path = folder.resolve("viewer.json")
    private var now = Instant.parse("2026-11-14T12:00:00Z")
    private val nico = Viewer("nchieppa", setOf(ReviewRequest.Team("lumen", "auth")), "Nicholas Chieppa", "https://avatars.githubusercontent.com/u/1?v=4")
    private val other = Viewer("mfischer")

    private fun cache() = ViewerCache(ViewerFile(path), clock = { now })

    private class Counting(var answer: () -> Viewer) {
        var calls = 0

        suspend fun fetch(): Viewer {
            calls++
            return answer()
        }
    }

    @Test
    fun githubReadsTheViewersNamePictureAndTeamsOncePerToken() = runTest {
        val identity = """{"data":{"viewer":{"login":"nchieppa","name":"Nicholas Chieppa","avatarUrl":"https://avatars.githubusercontent.com/u/1?v=4"}}}"""
        val teams = """{"data":{"viewer":{"organizations":{"nodes":[{"login":"lumen","teams":{"nodes":[{"slug":"auth"}]}}]}}}}"""
        val transport = RecordedTransport(mutableListOf(ApiResponse(200, body = identity), ApiResponse(200, body = teams)))
        val github = GitHub(FakeGitHub.client(transport), viewers = cache())
        assertNull(github.knownViewer())
        assertEquals(nico, github.viewer())
        assertEquals(nico, github.viewer())
        assertEquals(nico, github.knownViewer())
        assertEquals(2, transport.requests.size)
        assertTrue("avatarUrl" in transport.requests.first().body!!)
    }

    @Test
    fun withoutATokenNoOneIsKnownAndNothingIsSent() = runTest {
        val transport = RecordedTransport(mutableListOf())
        val github = GitHub(FakeGitHub.client(transport, token = null), viewers = cache())
        assertNull(github.knownViewer())
        assertEquals(GitHubException.Reason.NoToken, assertFailsWith<GitHubException> { github.viewer() }.reason)
        assertTrue(transport.requests.isEmpty())
    }

    @Test
    fun anotherTokenIsReadAgain() = runTest {
        val cache = cache()
        val fetch = Counting { nico }
        assertEquals(nico, cache.viewer("one") { fetch.fetch() })
        fetch.answer = { other }
        assertEquals(other, cache.viewer("two") { fetch.fetch() })
        assertEquals(2, fetch.calls)
        assertNull(cache.known("one"))
        assertEquals(other, cache.known("two"))
    }

    @Test
    fun theViewerIsKeptOnDiskByTheTokensFingerprintAlone() = runTest {
        cache().viewer("secret-token") { nico }
        val text = Files.readString(path)
        assertFalse("secret-token" in text)
        val fetch = Counting { other }
        val later = cache()
        assertEquals(nico, later.known("secret-token"))
        assertEquals(nico, later.viewer("secret-token") { fetch.fetch() })
        assertEquals(0, fetch.calls)
        assertNull(later.known("another-token"))
    }

    @Test
    fun aDayLaterTheViewerIsReadAgainAndAFailureKeepsTheOldOne() = runTest {
        val cache = cache()
        cache.viewer("t") { nico }
        now += Duration.ofHours(25)
        assertEquals(nico, cache.viewer("t") { throw GitHubException(GitHubException.Reason.Failed, "offline") })
        val renamed = nico.copy(name = "Nico")
        assertEquals(renamed, cache.viewer("t") { renamed })
        assertEquals(renamed, cache().known("t"))
    }

    @Test
    fun aRefusedTokenIsNotAnsweredFromTheCache() = runTest {
        val cache = cache()
        cache.viewer("t") { nico }
        now += Duration.ofHours(25)
        val error = assertFailsWith<GitHubException> { cache.viewer("t") { throw GitHubException(GitHubException.Reason.Unauthorized, "no") } }
        assertEquals(GitHubException.Reason.Unauthorized, error.reason)
    }

    @Test
    fun anUnreadableFileIsIgnored() = runTest {
        Files.writeString(path, "not json")
        assertNull(cache().known("t"))
        assertEquals(nico, cache().viewer("t") { nico })
    }
}

package dev.chieppa.arc.host.github.notifications

import dev.chieppa.arc.host.github.FakeGitHub
import dev.chieppa.arc.host.github.RecordedTransport
import dev.chieppa.arc.host.github.api.ApiResponse
import dev.chieppa.arc.host.github.repo.GitHubRepo
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class NotificationsFeedTest {
    private val body = FakeGitHub.resource("notifications.json")

    @Test
    fun theFirstPollLearnsWhatIsThereAndLaterOnesAreConditional() = runTest {
        val changed = body.replace("2026-11-06T11:00:00Z", "2026-11-06T12:00:00Z")
        val transport = RecordedTransport(
            mutableListOf(
                ApiResponse(200, mapOf("etag" to "\"e1\"", "x-poll-interval" to "90"), body),
                ApiResponse(304),
                ApiResponse(200, mapOf("etag" to "\"e2\""), changed),
            ),
        )
        val feed = NotificationsFeed(FakeGitHub.client(transport))
        assertEquals(emptyList(), feed.poll())
        assertEquals(90, feed.pollSeconds)
        assertEquals(emptyList(), feed.poll())
        assertEquals("\"e1\"", transport.requests[1].headers["If-None-Match"])
        val fresh = feed.poll()
        assertEquals(listOf("102"), fresh.map { it.id })
        assertEquals(GitHubRepo("lumen", "web"), fresh.single().repo)
        assertEquals(91, fresh.single().number)
        assertEquals(NotificationThread.MENTION, fresh.single().reason)
    }

    @Test
    fun aNewUnreadThreadIsFreshAndAReadOneIsNot() {
        val threads = NotificationsParser.parse(FakeGitHub.client(RecordedTransport(mutableListOf())).parse(body))
        val (fresh) = NotificationsFeed.changes(mapOf("101" to threads[0].updatedAt), threads + threads[1].copy(id = "103", unread = false))
        assertEquals(listOf("102"), fresh.map { it.id })
    }
}

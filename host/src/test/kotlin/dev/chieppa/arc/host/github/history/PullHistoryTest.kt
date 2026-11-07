package dev.chieppa.arc.host.github.history

import dev.chieppa.arc.host.github.FakeGitHub
import dev.chieppa.arc.host.github.GitHub
import dev.chieppa.arc.host.github.RecordedTransport
import dev.chieppa.arc.host.github.api.ApiResponse
import dev.chieppa.arc.host.github.pulls.CheckState
import dev.chieppa.arc.host.github.repo.GitHubRepo
import kotlinx.coroutines.test.runTest
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PullHistoryTest {
    private val repo = GitHubRepo("cli", "cli")

    private fun github(body: String): Pair<GitHub, RecordedTransport> {
        val transport = RecordedTransport(mutableListOf(ApiResponse(200, body = body)))
        return GitHub(FakeGitHub.client(transport)) to transport
    }

    @Test
    fun aPullRequestsHistoryHasItsForcePushesAndReviewedCommits() = runTest {
        val (github, transport) = github(FakeGitHub.resource("pull-history.json"))
        val history = github.pullHistory(repo, 14953)!!
        val request = transport.requests.single().body!!
        assertTrue("pullRequest(number: 14953)" in request)
        assertTrue("itemTypes: [HEAD_REF_FORCE_PUSHED_EVENT]" in request)
        assertEquals("d675e340e79dde5430b624a7f1dafb7946b61c0e", history.head)
        assertEquals(
            listOf(
                ForcePush("7ab99285057425a0b4bd3838dbea45f5cb2ad0e4", "c302c46a1850782d74dfbb1304f36b9850b2e4a9", Instant.parse("2026-11-06T02:10:12Z")),
                ForcePush("c302c46a1850782d74dfbb1304f36b9850b2e4a9", "d675e340e79dde5430b624a7f1dafb7946b61c0e", Instant.parse("2026-11-06T06:21:17Z")),
            ),
            history.forcePushes,
        )
        assertEquals(
            listOf("7ab99285057425a0b4bd3838dbea45f5cb2ad0e4", "c302c46a1850782d74dfbb1304f36b9850b2e4a9", "d675e340e79dde5430b624a7f1dafb7946b61c0e"),
            history.reviews,
        )
    }

    @Test
    fun aMissingPullRequestHasNoHistory() = runTest {
        val (github) = github("""{"data":{"repository":{"pullRequest":null}}}""")
        assertNull(github.pullHistory(repo, 1))
    }

    @Test
    fun checksOfSeveralCommitsComeInOneRequest() = runTest {
        val (github, transport) = github(FakeGitHub.resource("commit-checks.json"))
        val oids = listOf("d675e340e79dde5430b624a7f1dafb7946b61c0e", "c302c46a1850782d74dfbb1304f36b9850b2e4a9", "7ab99285057425a0b4bd3838dbea45f5cb2ad0e4")
        val checks = github.commitChecks(repo, oids)
        val body = transport.requests.single().body!!
        assertTrue("c2: object(oid: " in body && "7ab99285057425a0b4bd3838dbea45f5cb2ad0e4" in body)
        assertEquals(oids.associateWith { CheckState.Passed }, checks)
    }
}

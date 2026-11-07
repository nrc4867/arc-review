package dev.chieppa.arc.host.github.pulls

import dev.chieppa.arc.host.github.FakeGitHub
import dev.chieppa.arc.host.github.GitHub
import dev.chieppa.arc.host.github.RecordedTransport
import dev.chieppa.arc.host.github.api.ApiResponse
import dev.chieppa.arc.host.github.repo.GitHubRepo
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PullRequestQueryTest {
    @Test
    fun onePullRequestReadsWhetherItIsMerged() = runTest {
        val transport = RecordedTransport(mutableListOf(ApiResponse(200, body = FakeGitHub.resource("pull-request.json"))))
        val pull = GitHub(FakeGitHub.client(transport)).pullRequest(GitHubRepo("cli", "cli"), 14941)!!
        assertTrue("pullRequest(number: 14941)" in transport.requests.single().body!!)
        assertTrue(" state " in transport.requests.single().body!!)
        assertEquals(14941, pull.number)
        assertEquals(PullState.Merged, pull.state)
        assertTrue(pull.state.ended)
    }

    @Test
    fun githubsStateNamesRead() {
        assertEquals(PullState.Open, PullState.parse("OPEN"))
        assertEquals(PullState.Merged, PullState.parse("MERGED"))
        assertEquals(PullState.Closed, PullState.parse("CLOSED"))
        assertEquals(PullState.Open, PullState.parse(null))
        assertFalse(PullState.Open.ended)
        assertTrue(PullState.Closed.ended)
    }
}

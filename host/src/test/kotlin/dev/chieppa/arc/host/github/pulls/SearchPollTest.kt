package dev.chieppa.arc.host.github.pulls

import dev.chieppa.arc.host.github.FakeGitHub
import dev.chieppa.arc.host.github.GitHub
import dev.chieppa.arc.host.github.RecordedTransport
import dev.chieppa.arc.host.github.api.ApiResponse
import dev.chieppa.arc.host.github.api.GitHubException
import dev.chieppa.arc.host.github.repo.GitHubRepo
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class SearchPollTest {
    private fun github(vararg answers: ApiResponse, token: String? = FakeGitHub.TOKEN): Pair<GitHub, RecordedTransport> {
        val transport = RecordedTransport(answers.toMutableList())
        return GitHub(FakeGitHub.client(transport, token)) to transport
    }

    @Test
    fun everySearchGoesInOneDocumentAndComesBackByKey() = runTest {
        val (github, transport) = github(ApiResponse(200, body = FakeGitHub.resource("search.json")))
        val poll = github.search(linkedMapOf("cli" to "is:open repo:cli/cli", "mine" to "is:pr author:@me"))
        assertEquals(1, transport.requests.size)
        val request = transport.requests.single()
        assertEquals("POST", request.method)
        assertEquals("Bearer ${FakeGitHub.TOKEN}", request.headers["Authorization"])
        assertTrue("s0: search" in request.body!! && "s1: search" in request.body!!)
        assertTrue("is:pr is:open repo:cli/cli" in request.body!!)

        val cli = poll.results.getValue("cli")
        assertEquals(59, cli.total)
        assertEquals(listOf(14964, 14963, 14927), cli.pulls.map { it.number })
        assertEquals(emptyList(), poll.results.getValue("mine").pulls)
        assertEquals(5000, poll.budget?.limit)
        assertEquals(4997, poll.budget?.remaining)
    }

    @Test
    fun aPullRequestReadsItsLabelsChecksReviewsAndThreads() = runTest {
        val (github) = github(ApiResponse(200, body = FakeGitHub.resource("search.json")))
        val pulls = github.search(mapOf("cli" to "repo:cli/cli")).results.getValue("cli").pulls
        val first = pulls[0]
        assertEquals(GitHubRepo("cli", "cli"), first.repo)
        assertEquals("cli/cli#14964", first.key)
        assertEquals("dependabot", first.author)
        assertEquals(listOf("dependencies", "github_actions"), first.labels.map { it.name })
        assertEquals("0366d6", first.labels[0].color)
        assertEquals(31, first.comments)
        assertEquals(CheckState.Passed, first.checks.state)
        assertEquals(listOf(ReviewRequest.User("babakks")), first.requests)
        assertEquals("trunk", first.baseBranch)

        val failing = pulls[2]
        assertEquals(CheckState.Failed, failing.checks.state)
        assertEquals("build (windows-latest)", failing.checks.failed.first())
        assertEquals(4, failing.unresolvedThreads)
    }

    @Test
    fun withoutATokenNothingIsSent() = runTest {
        val (github, transport) = github(token = null)
        val error = assertFailsWith<GitHubException> { github.search(mapOf("a" to "is:open")) }
        assertEquals(GitHubException.Reason.NoToken, error.reason)
        assertEquals(0, transport.requests.size)
    }

    @Test
    fun graphQlErrorsFail() = runTest {
        val (github) = github(ApiResponse(200, body = """{"data":null,"errors":[{"message":"bad"}]}"""))
        assertFailsWith<GitHubException> { github.search(mapOf("a" to "is:open")) }
    }

    @Test
    fun aRefusedTokenFailsAsUnauthorized() = runTest {
        val (github) = github(ApiResponse(401))
        assertEquals(GitHubException.Reason.Unauthorized, assertFailsWith<GitHubException> { github.viewer() }.reason)
    }

    @Test
    fun queriesAreQuotedAndTyped() {
        assertEquals("\"a \\\"b\\\" \\\\ c\"", SearchDocument.quote("a \"b\" \\ c"))
        assertEquals("is:pr is:open author:@me", SearchDocument.withType("is:open author:@me"))
        assertEquals("is:issue label:bug", SearchDocument.withType("is:issue label:bug"))
        assertEquals("type:pr x", SearchDocument.withType("type:pr x"))
    }
}

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
        assertEquals(PullState.Open, first.state)

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

    private fun one(count: Int) = ApiResponse(200, body = """{"data":{"rateLimit":{"limit":5000,"remaining":4990,"resetAt":"2026-11-14T14:00:00Z"},"s0":{"issueCount":$count,"nodes":[]}}}""")

    @Test
    fun searchesGitHubGivesUpOnAreAskedAgainOneByOneWithSmallerPages() = runTest {
        val (github, transport) = github(ApiResponse(502), one(7), ApiResponse(504), ApiResponse(502))
        val poll = github.search(linkedMapOf("team" to "org:big", "mine" to "author:@me"))
        assertEquals(7, poll.results.getValue("team").total)
        assertEquals(setOf("mine"), poll.failed.keys)
        assertEquals(4990, poll.budget?.remaining)
        assertEquals(4, transport.requests.size)
        assertTrue("first: 20" in transport.requests[1].body!! && "org:big" in transport.requests[1].body!!)
        assertTrue("first: 20" in transport.requests[2].body!! && "author:@me" in transport.requests[2].body!!)
        assertTrue("first: 10" in transport.requests[3].body!!)
    }

    @Test
    fun aPollWhereEverySearchTimesOutFails() = runTest {
        val (github) = github(ApiResponse(502), ApiResponse(502), ApiResponse(502))
        val failure = assertFailsWith<GitHubException> { github.search(mapOf("team" to "org:big")) }
        assertTrue(failure.timedOut)
    }

    @Test
    fun anotherFailureIsNotAskedAgain() = runTest {
        val (github, transport) = github(ApiResponse(500))
        assertFailsWith<GitHubException> { github.search(mapOf("team" to "org:big", "mine" to "author:@me")) }
        assertEquals(1, transport.requests.size)
    }
}

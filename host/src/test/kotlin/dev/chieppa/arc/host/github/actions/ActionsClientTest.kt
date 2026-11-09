package dev.chieppa.arc.host.github.actions

import dev.chieppa.arc.core.actions.RunState
import dev.chieppa.arc.core.actions.RunStatus
import dev.chieppa.arc.host.github.FakeGitHub
import dev.chieppa.arc.host.github.GitHub
import dev.chieppa.arc.host.github.RecordedTransport
import dev.chieppa.arc.host.github.api.ApiResponse
import dev.chieppa.arc.host.github.api.GitHubApi
import dev.chieppa.arc.host.github.repo.GitHubRepo
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ActionsClientTest {
    private val repo = GitHubRepo("lumen", "api")

    @Test
    fun `reads a repo's runs and asks again conditionally`() = runTest {
        val transport = RecordedTransport(
            mutableListOf(
                ApiResponse(200, mapOf("etag" to "W/\"1\"", "x-ratelimit-limit" to "5000", "x-ratelimit-remaining" to "4712", "x-ratelimit-resource" to "core"), FakeGitHub.resource("actions-runs.json")),
                ApiResponse(304),
            ),
        )
        val github = GitHub(FakeGitHub.client(transport))
        val first = github.actions.runs(repo, null)
        val runs = first.value!!
        assertEquals(listOf(2291L, 2300L), runs.map { it.id })
        assertEquals(RunStatus.Waiting, runs[0].status)
        assertEquals(".github/workflows/ci.yml", runs[1].workflowPath)
        assertTrue(runs[1].awaitsForkApproval)
        assertEquals(RunState.Waiting, runs[1].state)
        assertEquals("jpark", runs[0].actor)
        assertEquals(4712, github.restBudget?.remaining)

        val second = github.actions.runs(repo, first.etag)
        assertTrue(second.unchanged)
        assertEquals("W/\"1\"", transport.requests[1].headers[GitHubApi.IF_NONE_MATCH])
        assertTrue(transport.requests[0].url.endsWith("/repos/lumen/api/actions/runs?per_page=50"))
    }

    @Test
    fun `reads jobs and their steps`() = runTest {
        val transport = RecordedTransport(mutableListOf(ApiResponse(200, body = FakeGitHub.resource("actions-jobs.json"))))
        val jobs = GitHub(FakeGitHub.client(transport)).actions.jobs(repo, 2291, null).value!!
        val job = jobs.single()
        assertEquals("deploy · staging", job.name)
        assertEquals(listOf("Set up job", "Migrate staging"), job.steps.map { it.name })
        assertEquals(24L, job.steps[1].seconds(0))
        assertEquals(listOf("ubuntu-24.04"), job.labels)
    }

    @Test
    fun `reads pending deployments with who may approve`() = runTest {
        val transport = RecordedTransport(mutableListOf(ApiResponse(200, body = FakeGitHub.resource("actions-pending.json"))))
        val pending = GitHub(FakeGitHub.client(transport)).actions.pendingDeployments(repo, 2291).single()
        assertEquals("production", pending.environment)
        assertTrue(pending.canApprove)
        assertEquals(listOf("nc", "deployers"), pending.reviewers)
    }

    @Test
    fun `streams a job's log`() = runTest {
        val transport = RecordedTransport(mutableListOf(ApiResponse(200, body = "line 1\nline 2\n"), ApiResponse(404)))
        val actions = GitHub(FakeGitHub.client(transport)).actions
        assertEquals(2, actions.jobLog(repo, 501) { it.bufferedReader().lineSequence().count() })
        assertNull(actions.jobLog(repo, 502) { 1 })
    }

    @Test
    fun `reads what a fork changes under github`() = runTest {
        val transport = RecordedTransport(
            mutableListOf(
                ApiResponse(200, body = """[{"number": 93}]"""),
                ApiResponse(200, body = """[{"filename": ".github/workflows/ci.yml", "status": "modified", "patch": "@@ -1 +1 @@"}, {"filename": "README.md", "status": "modified"}]"""),
            ),
        )
        val changes = GitHub(FakeGitHub.client(transport)).actions.forkChanges(GitHubRepo("lumen", "web"), "tmoreau", "patch-1")!!
        assertEquals(93, changes.pull)
        assertEquals(listOf(".github/workflows/ci.yml"), changes.changes.map { it.path })
        assertTrue("head=tmoreau%3Apatch-1" in transport.requests[0].url)
    }

    @Test
    fun `reads what a deployment ships since the last one`() = runTest {
        val transport = RecordedTransport(
            mutableListOf(
                ApiResponse(200, body = """[{"sha": "7d41e0a"}, {"sha": "1111111"}]"""),
                ApiResponse(200, body = """{"total_commits": 2, "commits": [{"sha": "a", "commit": {"message": "Session list endpoint (#405)\n\nbody"}}, {"sha": "b", "commit": {"message": "Merge pull request #409 from x/y"}}]}"""),
            ),
        )
        val ships = GitHub(FakeGitHub.client(transport)).actions.shipsSince(repo, "production", "7d41e0a")!!
        assertEquals("1111111", ships.base)
        assertEquals(listOf(409, 405), ships.changes.map { it.pull })
        assertTrue(transport.requests[1].url.endsWith("/compare/1111111...7d41e0a"))
    }
}

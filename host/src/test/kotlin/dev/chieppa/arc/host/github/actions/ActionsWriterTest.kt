package dev.chieppa.arc.host.github.actions

import dev.chieppa.arc.host.github.FakeGitHub
import dev.chieppa.arc.host.github.RecordedTransport
import dev.chieppa.arc.host.github.api.ApiResponse
import dev.chieppa.arc.host.github.api.GitHubException
import dev.chieppa.arc.host.github.repo.GitHubRepo
import dev.chieppa.arc.host.github.review.MutationPacer
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class ActionsWriterTest {
    private val repo = GitHubRepo("lumen", "api")
    private fun writer(transport: RecordedTransport) = ActionsWriter(FakeGitHub.client(transport), MutationPacer(0, { 0 }, {}))

    @Test
    fun `a dispatch sends its inputs and reads the new run's id`() = runTest {
        val transport = RecordedTransport(mutableListOf(ApiResponse(200, body = """{"workflow_run_id": 2302, "run_url": "u", "html_url": "h"}""")))
        val result = writer(transport).dispatch(repo, "speckit-implement.yml", "main", mapOf("issue" to "431", "dry_run" to "false"))
        assertEquals(2302L, result.runId)
        val request = transport.requests.single()
        assertEquals("POST", request.method)
        assertEquals("https://api.github.com/repos/lumen/api/actions/workflows/speckit-implement.yml/dispatches", request.url)
        val body = Json.parseToJsonElement(request.body!!).jsonObject
        assertEquals("main", body.getValue("ref").jsonPrimitive.content)
        assertEquals("431", body.getValue("inputs").jsonObject.getValue("issue").jsonPrimitive.content)
        assertEquals("true", body.getValue("return_run_details").jsonPrimitive.content)
    }

    @Test
    fun `a dispatch answered with no content has no run id`() = runTest {
        assertNull(writer(RecordedTransport(mutableListOf(ApiResponse(204)))).dispatch(repo, "ci.yml", "main", emptyMap()).runId)
    }

    @Test
    fun `a deployment review names its environments and state`() = runTest {
        val transport = RecordedTransport(mutableListOf(ApiResponse(200, body = "[]")))
        writer(transport).reviewDeployments(repo, 2291, listOf(161088068), approve = true, comment = "V41 checked")
        val body = Json.parseToJsonElement(transport.requests.single().body!!).jsonObject
        assertEquals("approved", body.getValue("state").jsonPrimitive.content)
        assertEquals("[161088068]", body.getValue("environment_ids").toString())
        assertEquals("V41 checked", body.getValue("comment").jsonPrimitive.content)
    }

    @Test
    fun `a refused write throws`() = runTest {
        val transport = RecordedTransport(mutableListOf(ApiResponse(409)))
        assertFailsWith<GitHubException> { writer(transport).cancel(repo, 1) }
        assertEquals("https://api.github.com/repos/lumen/api/actions/runs/1/cancel", transport.requests.single().url)
    }

    @Test
    fun `re-runs ask for debug logging when wanted`() = runTest {
        val transport = RecordedTransport(mutableListOf(ApiResponse(201), ApiResponse(201)))
        writer(transport).rerunFailed(repo, 9, debug = true)
        writer(transport).rerunJob(repo, 77, debug = false)
        assertEquals("""{"enable_debug_logging":true}""", transport.requests[0].body)
        assertEquals("https://api.github.com/repos/lumen/api/actions/jobs/77/rerun", transport.requests[1].url)
    }
}

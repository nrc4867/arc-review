package dev.chieppa.arc.host.github.api

import dev.chieppa.arc.core.activity.ActivityKind
import dev.chieppa.arc.core.activity.ActivityLog
import dev.chieppa.arc.core.activity.ActivityOutcome
import dev.chieppa.arc.host.github.FakeGitHub
import dev.chieppa.arc.host.github.RecordedTransport
import kotlinx.coroutines.test.runTest
import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ReportingTransportTest {
    private val log = ActivityLog()

    private fun client(vararg answers: ApiResponse) = FakeGitHub.client(ReportingTransport(RecordedTransport(answers.toMutableList()), log))

    @Test
    fun `a request is reported by method, path and status without its headers`() = runTest {
        client(ApiResponse(200, body = "[]")).get("${GitHubApi.REPOS}/o/n/pulls?state=open")
        val entry = log.entries().single()
        assertEquals(ActivityKind.GitHub, entry.kind)
        assertEquals("GET /repos/o/n/pulls?state=open", entry.title)
        assertEquals("api.github.com", entry.place)
        assertEquals("200", entry.code)
        assertEquals(ActivityOutcome.Succeeded, entry.outcome)
        assertFalse(log.entries().any { FakeGitHub.TOKEN in it.toString() })
    }

    @Test
    fun `an error status is a failure with the start of its body`() = runTest {
        val body = """{"message":"Resource protected by organization SAML enforcement."}"""
        client(ApiResponse(403, body = body)).get("${GitHubApi.REPOS}/company/private")
        val entry = log.entries().single()
        assertEquals(ActivityOutcome.Failed, entry.outcome)
        assertEquals("403", entry.code)
        assertEquals(body, entry.error)
    }

    @Test
    fun `graphql errors under a 200 are a failure`() = runTest {
        assertFailsWith<GitHubException> { client(ApiResponse(200, body = """{"errors":[{"message":"Bad"}]}""")).graphql("query { viewer { login } }") }
        assertEquals(ActivityOutcome.Failed, log.entries().single().outcome)
    }

    @Test
    fun `a not modified answer is a success`() = runTest {
        client(ApiResponse(304)).get(GitHubApi.NOTIFICATIONS, etag = "x")
        assertNull(log.entries().single().error)
        assertEquals(ActivityOutcome.Succeeded, log.entries().single().outcome)
    }

    @Test
    fun `a request with no answer is a failure with the reason`() = runTest {
        val transport = ReportingTransport({ throw IOException("Connection refused") }, log)
        assertFailsWith<GitHubException> { FakeGitHub.client(transport).get(GitHubApi.NOTIFICATIONS) }
        val entry = log.entries().single()
        assertEquals(ActivityOutcome.Failed, entry.outcome)
        assertTrue(entry.error!!.contains("Connection refused"))
    }
}

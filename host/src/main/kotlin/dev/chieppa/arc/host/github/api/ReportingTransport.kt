package dev.chieppa.arc.host.github.api

import dev.chieppa.arc.core.activity.ActivityEntry
import dev.chieppa.arc.core.activity.ActivityKind
import dev.chieppa.arc.core.activity.ActivityOutcome
import dev.chieppa.arc.core.activity.ActivityReporter
import dev.chieppa.arc.core.activity.elapsedMillis
import dev.chieppa.arc.core.activity.failureText
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import java.io.IOException
import java.io.InputStream
import java.net.URI
import java.time.Instant
import java.util.concurrent.CancellationException

/**
 * [transport] reporting every request to [activity]: its method and path, host, status and time, and for a failure
 * (no answer, a status GitHub gives for an error, or GraphQL errors) the start of the body. Headers, where the token
 * is, are never reported.
 */
class ReportingTransport(private val transport: HttpTransport, private val activity: ActivityReporter) : HttpTransport {
    override suspend fun send(request: ApiRequest): ApiResponse {
        val started = Instant.now()
        val start = System.nanoTime()
        val response = attempt(request, started, start) { transport.send(request) }
        val failed = response.status !in SUCCESS && response.status != GitHubApi.NOT_MODIFIED || graphqlErrors(request, response)
        report(request, started, start, if (failed) ActivityOutcome.Failed else ActivityOutcome.Succeeded, response.status.toString(), if (failed) snippet(response.body) else null)
        return response
    }

    override suspend fun <T> stream(request: ApiRequest, read: (Int, InputStream) -> T): T {
        val started = Instant.now()
        val start = System.nanoTime()
        var answered = false
        return attempt(request, started, start, { answered }) {
            transport.stream(request) { status, input ->
                answered = true
                val failed = status !in SUCCESS
                report(request, started, start, if (failed) ActivityOutcome.Failed else ActivityOutcome.Succeeded, status.toString(), null)
                read(status, input)
            }
        }
    }

    /** Runs [block], reporting a request that ends without an answer; once [answered], the answer was reported already. */
    private inline fun <T> attempt(request: ApiRequest, started: Instant, start: Long, answered: () -> Boolean = { false }, block: () -> T): T = try {
        block()
    } catch (e: CancellationException) {
        if (!answered()) report(request, started, start, ActivityOutcome.Cancelled, null, null)
        throw e
    } catch (e: IOException) {
        if (!answered()) report(request, started, start, ActivityOutcome.Failed, null, failureText(e))
        throw e
    }

    private fun report(request: ApiRequest, started: Instant, start: Long, outcome: ActivityOutcome, code: String?, error: String?) {
        val uri = runCatching { URI(request.url) }.getOrNull()
        val path = uri?.let { it.rawPath + (it.rawQuery?.let { query -> "?$query" } ?: "") } ?: request.url
        activity.report(ActivityEntry(ActivityKind.GitHub, "${request.method} $path", outcome, started, elapsedMillis(start), uri?.host, code, error))
    }

    private fun graphqlErrors(request: ApiRequest, response: ApiResponse): Boolean {
        if (request.url != GitHubApi.GRAPHQL || response.status != GitHubApi.OK || ERRORS !in response.body) return false
        val root = runCatching { json.parseToJsonElement(response.body) }.getOrNull() as? JsonObject ?: return false
        return !(root[ERRORS] as? JsonArray).isNullOrEmpty()
    }

    private fun snippet(body: String): String = body.trim().let { if (it.length > SNIPPET) it.take(SNIPPET) + ELLIPSIS else it }

    private companion object {
        val SUCCESS = 200..299
        const val SNIPPET = 600
        const val ELLIPSIS = "…"
        const val ERRORS = "errors"
        val json = Json { ignoreUnknownKeys = true }
    }
}

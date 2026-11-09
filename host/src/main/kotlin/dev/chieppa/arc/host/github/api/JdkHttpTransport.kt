package dev.chieppa.arc.host.github.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.await
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

/** [HttpTransport] through `java.net.http`; cancelling the caller cancels the request. */
class JdkHttpTransport(private val client: HttpClient = HttpClient.newBuilder().connectTimeout(TIMEOUT).build()) : HttpTransport {
    override suspend fun send(request: ApiRequest): ApiResponse {
        val response = client.sendAsync(build(request), HttpResponse.BodyHandlers.ofString()).await()
        return ApiResponse(response.statusCode(), headers(response), response.body().orEmpty())
    }

    override suspend fun <T> stream(request: ApiRequest, read: (Int, InputStream) -> T): T {
        var response = client.sendAsync(build(request), HttpResponse.BodyHandlers.ofInputStream()).await()
        val location = response.headers().firstValue(LOCATION).orElse(null)
        if (response.statusCode() in REDIRECTS && location != null) {
            response.body().close()
            val plain = request.headers.filterKeys { it != GitHubApi.AUTHORIZATION }
            response = client.sendAsync(build(ApiRequest(GitHubApi.GET, location, plain)), HttpResponse.BodyHandlers.ofInputStream()).await()
        }
        return withContext(Dispatchers.IO) { response.body().use { read(response.statusCode(), it) } }
    }

    private fun build(request: ApiRequest): HttpRequest {
        val builder = HttpRequest.newBuilder(URI(request.url)).timeout(TIMEOUT)
        request.headers.forEach { (name, value) -> builder.header(name, value) }
        val body = request.body?.let(HttpRequest.BodyPublishers::ofString) ?: HttpRequest.BodyPublishers.noBody()
        builder.method(request.method, body)
        return builder.build()
    }

    private fun headers(response: HttpResponse<*>): Map<String, String> =
        response.headers().map().entries.associate { (name, values) -> name.lowercase() to values.firstOrNull().orEmpty() }

    private companion object {
        val TIMEOUT: Duration = Duration.ofSeconds(30)
        val REDIRECTS = setOf(301, 302, 303, 307, 308)
        const val LOCATION = "location"
    }
}

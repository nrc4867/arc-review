package dev.chieppa.arc.host.github.api

import kotlinx.coroutines.future.await
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

/** [HttpTransport] through `java.net.http`; cancelling the caller cancels the request. */
class JdkHttpTransport(private val client: HttpClient = HttpClient.newBuilder().connectTimeout(TIMEOUT).build()) : HttpTransport {
    override suspend fun send(request: ApiRequest): ApiResponse {
        val builder = HttpRequest.newBuilder(URI(request.url)).timeout(TIMEOUT)
        request.headers.forEach { (name, value) -> builder.header(name, value) }
        val body = request.body?.let(HttpRequest.BodyPublishers::ofString) ?: HttpRequest.BodyPublishers.noBody()
        builder.method(request.method, body)
        val response = client.sendAsync(builder.build(), HttpResponse.BodyHandlers.ofString()).await()
        val headers = response.headers().map().entries.associate { (name, values) -> name.lowercase() to values.firstOrNull().orEmpty() }
        return ApiResponse(response.statusCode(), headers, response.body().orEmpty())
    }

    private companion object {
        val TIMEOUT: Duration = Duration.ofSeconds(30)
    }
}

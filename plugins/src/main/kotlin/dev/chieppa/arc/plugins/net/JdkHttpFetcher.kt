package dev.chieppa.arc.plugins.net

import dev.chieppa.arc.plugins.protocol.FetchParams
import dev.chieppa.arc.plugins.protocol.FetchResult
import kotlinx.coroutines.future.await
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

/**
 * Plugins' HTTP requests through the JDK's client: no redirects followed, so every host a request reaches is one the
 * gate checked, no cookies or credentials of Arc's, and at most [MAX_BYTES] of a body.
 */
object JdkHttpFetcher : HttpFetcher {
    private const val MAX_BYTES = 16 * 1024 * 1024
    private val CONNECT: Duration = Duration.ofSeconds(10)
    private val TIMEOUT: Duration = Duration.ofSeconds(30)
    private const val TOO_LARGE = "The answer is larger than a plugin may fetch"

    private val client: HttpClient = HttpClient.newBuilder()
        .followRedirects(HttpClient.Redirect.NEVER)
        .connectTimeout(CONNECT)
        .build()

    override suspend fun fetch(request: FetchParams): FetchResult {
        val built = try {
            HttpRequest.newBuilder(URI(request.url))
                .timeout(TIMEOUT)
                .method(request.method.uppercase(), request.body?.let(HttpRequest.BodyPublishers::ofString) ?: HttpRequest.BodyPublishers.noBody())
                .apply { request.headers.forEach { (name, value) -> header(name, value) } }
                .build()
        } catch (e: IllegalArgumentException) {
            throw IOException(e.message ?: e.toString(), e)
        }
        val response = client.sendAsync(built, HttpResponse.BodyHandlers.ofInputStream()).await()
        val bytes = response.body().use { it.readNBytes(MAX_BYTES + 1) }
        if (bytes.size > MAX_BYTES) throw IOException(TOO_LARGE)
        val headers = response.headers().map().mapValues { it.value.firstOrNull().orEmpty() }
        return FetchResult(response.statusCode(), headers, bytes.toString(Charsets.UTF_8))
    }
}

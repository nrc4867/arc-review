package dev.chieppa.arc.host.github.avatars

import dev.chieppa.arc.host.github.api.GitHubApi
import kotlinx.coroutines.future.await
import java.io.IOException
import java.net.URI
import java.net.URISyntaxException
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

/** [AvatarFetcher] through `java.net.http`, with no token: avatar addresses are public. */
class JdkAvatarFetcher(
    private val client: HttpClient = HttpClient.newBuilder().connectTimeout(TIMEOUT).followRedirects(HttpClient.Redirect.NORMAL).build(),
) : AvatarFetcher {
    override suspend fun fetch(url: String): ByteArray? = try {
        val request = HttpRequest.newBuilder(URI(url)).timeout(TIMEOUT).header(GitHubApi.USER_AGENT, GitHubApi.AGENT).GET().build()
        val response = client.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray()).await()
        if (response.statusCode() == GitHubApi.OK) response.body() else null
    } catch (_: IOException) {
        null
    } catch (_: URISyntaxException) {
        null
    } catch (_: IllegalArgumentException) {
        null
    }

    private companion object {
        val TIMEOUT: Duration = Duration.ofSeconds(15)
    }
}

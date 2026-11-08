package dev.chieppa.arc.host.github.media

import dev.chieppa.arc.host.github.api.GitHubApi
import kotlinx.coroutines.future.await
import java.io.IOException
import java.net.URI
import java.net.URISyntaxException
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

/**
 * [MediaFetcher] through `java.net.http`. The person's [token] goes only to GitHub's own hosts, as [MediaRules] says;
 * redirects are followed here, so a token never reaches the storage a redirect points to. Only https is fetched and
 * only a picture is kept.
 */
class JdkMediaFetcher(
    private val token: () -> String?,
    private val client: HttpClient = HttpClient.newBuilder().connectTimeout(TIMEOUT).followRedirects(HttpClient.Redirect.NEVER).build(),
) : MediaFetcher {
    override suspend fun fetch(url: String): ByteArray? = try {
        var at = url
        var answer: ByteArray? = null
        for (hop in 0..MediaRules.MAX_REDIRECTS) {
            if (!MediaRules.fetchable(at)) break
            val builder = HttpRequest.newBuilder(URI(at)).timeout(TIMEOUT).header(GitHubApi.USER_AGENT, GitHubApi.AGENT).GET()
            if (MediaRules.sendsToken(at)) token()?.let { builder.header(GitHubApi.AUTHORIZATION, GitHubApi.BEARER + it) }
            val response = client.sendAsync(builder.build(), HttpResponse.BodyHandlers.ofByteArray()).await()
            val location = response.headers().firstValue(LOCATION).orElse(null)
            if (response.statusCode() in REDIRECTS && location != null) {
                at = MediaRules.resolve(at, location)
                continue
            }
            val type = response.headers().firstValue(CONTENT_TYPE).orElse(null)
            if (response.statusCode() == GitHubApi.OK && MediaRules.keeps(type, response.body().size)) answer = response.body()
            break
        }
        answer
    } catch (_: IOException) {
        null
    } catch (_: URISyntaxException) {
        null
    } catch (_: IllegalArgumentException) {
        null
    }

    private companion object {
        val TIMEOUT: Duration = Duration.ofSeconds(20)
        val REDIRECTS = 300..399
        const val LOCATION = "location"
        const val CONTENT_TYPE = "content-type"
    }
}

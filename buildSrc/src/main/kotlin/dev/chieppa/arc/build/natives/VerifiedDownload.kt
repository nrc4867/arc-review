package dev.chieppa.arc.build.natives

import org.gradle.api.GradleException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Files
import java.nio.file.Path
import java.security.DigestInputStream
import java.security.MessageDigest
import java.time.Duration

/** Downloads a file and fails unless its SHA-256 is the expected one. */
object VerifiedDownload {
    private const val HTTP_OK = 200
    private val CONNECT_TIMEOUT: Duration = Duration.ofSeconds(30)

    fun fetch(url: String, sha256: String, target: Path) {
        val client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).connectTimeout(CONNECT_TIMEOUT).build()
        val response = client.send(HttpRequest.newBuilder(URI.create(url)).GET().build(), HttpResponse.BodyHandlers.ofInputStream())
        if (response.statusCode() != HTTP_OK) {
            response.body().close()
            throw GradleException("Downloading $url failed: HTTP ${response.statusCode()}")
        }
        val digest = MessageDigest.getInstance("SHA-256")
        DigestInputStream(response.body(), digest).use { input -> Files.newOutputStream(target).use { input.transferTo(it) } }
        val actual = digest.digest().joinToString("") { "%02x".format(it) }
        if (!actual.equals(sha256, ignoreCase = true)) {
            Files.deleteIfExists(target)
            throw GradleException("$url has SHA-256 $actual, not the pinned $sha256")
        }
    }
}

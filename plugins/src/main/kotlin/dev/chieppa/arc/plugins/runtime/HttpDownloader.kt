package dev.chieppa.arc.plugins.runtime

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runInterruptible
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Files
import java.nio.file.Path
import java.security.DigestInputStream
import java.security.MessageDigest
import java.time.Duration

/** Downloads over HTTPS, checking each file's SHA-256 and deleting one that does not match. */
object HttpDownloader : Downloader {
    private const val HTTP_OK = 200
    private const val SHA_256 = "SHA-256"
    private val CONNECT_TIMEOUT: Duration = Duration.ofSeconds(30)

    override suspend fun download(url: String, sha256: String, target: Path) = runInterruptible(Dispatchers.IO) {
        val client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).connectTimeout(CONNECT_TIMEOUT).build()
        val response = client.send(HttpRequest.newBuilder(URI.create(url)).GET().build(), HttpResponse.BodyHandlers.ofInputStream())
        if (response.statusCode() != HTTP_OK) {
            response.body().close()
            throw IOException("Downloading $url failed: HTTP ${response.statusCode()}")
        }
        val digest = MessageDigest.getInstance(SHA_256)
        DigestInputStream(response.body(), digest).use { input -> Files.newOutputStream(target).use { input.transferTo(it) } }
        val actual = digest.digest().joinToString("") { "%02x".format(it) }
        if (!actual.equals(sha256, ignoreCase = true)) {
            Files.deleteIfExists(target)
            throw IOException("$url has SHA-256 $actual, not the pinned $sha256")
        }
    }
}

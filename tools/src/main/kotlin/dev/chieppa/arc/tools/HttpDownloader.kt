package dev.chieppa.arc.tools

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runInterruptible
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
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
    private const val BUFFER = 64 * 1024
    private const val CONTENT_LENGTH = "content-length"
    private val CONNECT_TIMEOUT: Duration = Duration.ofSeconds(30)

    override suspend fun download(url: String, sha256: String, target: Path, progress: DownloadProgress) = runInterruptible(Dispatchers.IO) {
        val client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).connectTimeout(CONNECT_TIMEOUT).build()
        val response = client.send(HttpRequest.newBuilder(URI.create(url)).GET().build(), HttpResponse.BodyHandlers.ofInputStream())
        if (response.statusCode() != HTTP_OK) {
            response.body().close()
            throw IOException("Downloading $url failed: HTTP ${response.statusCode()}")
        }
        val total = response.headers().firstValueAsLong(CONTENT_LENGTH).takeIf { it.isPresent }?.asLong
        val digest = MessageDigest.getInstance(Sha256.ALGORITHM)
        DigestInputStream(response.body(), digest).use { input -> Files.newOutputStream(target).use { copy(input, it, total, progress) } }
        val actual = Sha256.hex(digest.digest())
        if (!actual.equals(sha256, ignoreCase = true)) {
            Files.deleteIfExists(target)
            throw IOException("$url has SHA-256 $actual, not the pinned $sha256")
        }
    }

    private fun copy(input: InputStream, output: OutputStream, total: Long?, progress: DownloadProgress) {
        val buffer = ByteArray(BUFFER)
        var done = 0L
        progress.bytes(done, total)
        while (true) {
            val count = input.read(buffer)
            if (count < 0) break
            output.write(buffer, 0, count)
            done += count
            progress.bytes(done, total)
        }
    }
}

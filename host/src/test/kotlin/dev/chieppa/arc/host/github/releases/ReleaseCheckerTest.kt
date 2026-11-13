package dev.chieppa.arc.host.github.releases

import dev.chieppa.arc.host.github.FakeGitHub
import dev.chieppa.arc.host.github.RecordedTransport
import dev.chieppa.arc.host.github.api.ApiResponse
import dev.chieppa.arc.tools.DownloadProgress
import dev.chieppa.arc.tools.Sha256
import kotlinx.coroutines.test.runTest
import java.io.IOException
import java.nio.file.Files
import java.security.MessageDigest
import kotlin.io.path.createTempDirectory
import kotlin.io.path.readText
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull

class ReleaseCheckerTest {
    private val dir = createTempDirectory("arc-releases")
    private val releases = ApiResponse(200, body = FakeGitHub.resource("releases.json"))

    @AfterTest
    fun cleanUp() {
        dir.toFile().deleteRecursively()
    }

    private fun checker(vararg answers: ApiResponse): Pair<ReleaseChecker, RecordedTransport> {
        val transport = RecordedTransport(answers.toMutableList())
        return ReleaseChecker(FakeGitHub.client(transport)) to transport
    }

    private fun windowsJar(version: String) = "arc-review-windows-x64-$version.jar"

    @Test
    fun offersTheNewestPublishedReleaseWithThisPlatformsJar() = runTest {
        val (checker, transport) = checker(releases, releases, releases, releases)
        val offer = checker.newer("0.2.0", ::windowsJar)
        assertEquals("0.3.0", offer?.version)
        assertEquals("https://api.github.com/repos/nrc4867/arc-review/releases/assets/2001", offer?.jar?.url)
        assertEquals("https://api.github.com/repos/nrc4867/arc-review/releases/assets/2003", offer?.sums?.url)
        assertEquals("https://api.github.com/repos/nrc4867/arc-review/releases?per_page=30", transport.requests.first().url)
        assertEquals("Bearer ${FakeGitHub.TOKEN}", transport.requests.first().headers["Authorization"])

        assertNull(checker.newer("0.3.0", ::windowsJar), "a prerelease is not offered to a release, nor a draft")
        assertEquals("0.4.0-beta.1", checker.newer("0.3.1-beta.1", ::windowsJar)?.version)
        assertNull(checker.newer("0.2.0") { "arc-review-linux-x64-$it.jar" })
    }

    @Test
    fun downloadsTheJarCheckedAgainstTheSums() = runTest {
        val jar = "jar bytes"
        val sums = "${sha(jar)}  ${windowsJar("0.3.0")}\n${sha("other")} *arc-review-macos-arm64-0.3.0.jar\n"
        val (checker, transport) = checker(releases, ApiResponse(200, body = sums), ApiResponse(200, body = jar))
        val offer = checker.newer("0.2.0", ::windowsJar)!!
        val seen = mutableListOf<Long>()
        val path = checker.download(offer, dir, DownloadProgress { done, _ -> seen += done })
        assertEquals(jar, path.readText())
        assertEquals(jar.length.toLong(), seen.last())
        assertEquals("application/octet-stream", transport.requests[1].headers["Accept"])
        assertEquals(offer.jar.url, transport.requests[2].url)
    }

    @Test
    fun aJarThatDoesNotMatchIsDeleted() = runTest {
        val sums = "${sha("expected")}  ${windowsJar("0.3.0")}\n"
        val (checker, _) = checker(releases, ApiResponse(200, body = sums), ApiResponse(200, body = "tampered"))
        val offer = checker.newer("0.2.0", ::windowsJar)!!
        assertFailsWith<IOException> { checker.download(offer, dir, DownloadProgress.None) }
        assertFalse(Files.exists(dir.resolve(windowsJar("0.3.0"))))
    }

    @Test
    fun readsSumsAsShaSumWritesThem() {
        val hex = "a".repeat(64)
        assertEquals(mapOf("x.jar" to hex, "y.jar" to hex), ReleaseParser.sums("$hex  x.jar\n$hex *y.jar\nnot a line\n"))
    }

    private fun sha(text: String) = Sha256.hex(MessageDigest.getInstance(Sha256.ALGORITHM).digest(text.toByteArray()))
}

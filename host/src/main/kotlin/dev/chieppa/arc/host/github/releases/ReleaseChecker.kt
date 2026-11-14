package dev.chieppa.arc.host.github.releases

import dev.chieppa.arc.host.github.api.GitHubApi
import dev.chieppa.arc.host.github.api.GitHubClient
import dev.chieppa.arc.host.github.api.GitHubException
import dev.chieppa.arc.tools.DownloadProgress
import dev.chieppa.arc.tools.SemVer
import dev.chieppa.arc.tools.Sha256
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.security.DigestOutputStream
import java.security.MessageDigest

/**
 * Arc's releases on GitHub, read with the person's token (the repo [repo] may be private): which newer release this
 * machine can update to, and downloading its jar checked against the release's `SHA256SUMS.txt`.
 */
class ReleaseChecker(private val client: GitHubClient, private val repo: String = ARC_REPO) {
    /** The releases, newest first as GitHub lists them. Throws [GitHubException] when GitHub gives none. */
    suspend fun releases(): List<Release> {
        val response = client.get("${GitHubApi.REPOS}/$repo/releases?per_page=$PAGE")
        if (response.status == NOT_FOUND) throw GitHubException(GitHubException.Reason.Failed, "GitHub shows this account no releases of $repo (404)")
        if (response.status != GitHubApi.OK) throw GitHubException(GitHubException.Reason.Failed, "Releases of $repo answered ${response.status}")
        return ReleaseParser.parse(client.parse(response.body))
    }

    /**
     * The newest published release above [current] with a jar named by [jarName] for its version and the sums file;
     * a prerelease only when [current] is one itself. Null when there is none.
     */
    suspend fun newer(current: String, jarName: (String) -> String): ReleaseOffer? {
        val running = SemVer.parse(current) ?: return null
        return releases().asSequence()
            .filter { !it.draft && (running.prerelease || !it.prerelease) }
            .mapNotNull { release -> SemVer.parse(release.version)?.let { it to release } }
            .filter { (version, _) -> version > running }
            .sortedByDescending { (version, _) -> version }
            .firstNotNullOfOrNull { (_, release) ->
                val jar = release.asset(jarName(release.version))
                val sums = release.asset(SUMS)
                if (jar != null && sums != null) ReleaseOffer(release.version, jar, sums) else null
            }
    }

    /**
     * Downloads [offer]'s jar into [folder], its progress going to [progress], and gives its path once its SHA-256 is
     * the one the release lists. Throws [IOException] when it cannot be had or does not match, leaving no jar.
     */
    suspend fun download(offer: ReleaseOffer, folder: Path, progress: DownloadProgress): Path {
        val sums = fetch(offer.sums) { it.readAllBytes().toString(Charsets.UTF_8) }
        val expected = ReleaseParser.sums(sums)[offer.jar.name] ?: throw IOException("$SUMS lists no ${offer.jar.name}")
        Files.createDirectories(folder)
        val target = folder.resolve(offer.jar.name)
        val digest = MessageDigest.getInstance(Sha256.ALGORITHM)
        fetch(offer.jar) { input ->
            DigestOutputStream(Files.newOutputStream(target), digest).use { output -> copy(input, output, offer.jar.size, progress) }
        }
        val actual = Sha256.hex(digest.digest())
        if (actual != expected) {
            Files.deleteIfExists(target)
            throw IOException("${offer.jar.name} has SHA-256 $actual, not the listed $expected")
        }
        return target
    }

    private suspend fun <T> fetch(asset: ReleaseAsset, read: (InputStream) -> T): T = try {
        client.stream(asset.url, OCTET_STREAM) { status, input ->
            if (status != GitHubApi.OK) throw IOException("${asset.name} answered $status")
            read(input)
        }
    } catch (e: GitHubException) {
        throw IOException(e.message, e)
    }

    private fun copy(input: InputStream, output: OutputStream, total: Long, progress: DownloadProgress) {
        val buffer = ByteArray(BUFFER)
        var done = 0L
        while (true) {
            val count = input.read(buffer)
            if (count < 0) break
            output.write(buffer, 0, count)
            done += count
            progress.bytes(done, total.takeIf { it > 0 })
        }
    }

    companion object {
        /** Where Arc is released. */
        const val ARC_REPO = "nrc4867/arc-review"

        /** The file each release lists its files' SHA-256 in. */
        const val SUMS = "SHA256SUMS.txt"
        private const val OCTET_STREAM = "application/octet-stream"
        private const val PAGE = 30
        private const val NOT_FOUND = 404
        private const val BUFFER = 64 * 1024
    }
}

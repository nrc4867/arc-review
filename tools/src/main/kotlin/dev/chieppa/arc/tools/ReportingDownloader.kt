package dev.chieppa.arc.tools

import dev.chieppa.arc.core.activity.ActivityKind
import dev.chieppa.arc.core.activity.ActivityReporter
import dev.chieppa.arc.core.activity.track
import java.net.URI
import java.nio.file.Path

/** [downloader] reporting each download to [activity]: its url, host, time and, when it fails, why. */
class ReportingDownloader(private val downloader: Downloader, private val activity: ActivityReporter) : Downloader {
    override suspend fun download(url: String, sha256: String, target: Path, progress: DownloadProgress) {
        val host = runCatching { URI(url).host }.getOrNull()
        activity.track(ActivityKind.Download, "$GET $url", host) { downloader.download(url, sha256, target, progress) }
    }

    private companion object {
        const val GET = "GET"
    }
}

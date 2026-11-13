package dev.chieppa.arc.tools

import java.nio.file.Path

/** Fetches a file to [target], failing with [java.io.IOException] unless its SHA-256 is [sha256]; how far it got goes to [progress]. */
fun interface Downloader {
    suspend fun download(url: String, sha256: String, target: Path, progress: DownloadProgress)
}

/** [Downloader.download] with no one following its progress. */
suspend fun Downloader.download(url: String, sha256: String, target: Path) = download(url, sha256, target, DownloadProgress.None)

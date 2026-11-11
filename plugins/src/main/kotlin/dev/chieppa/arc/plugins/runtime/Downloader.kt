package dev.chieppa.arc.plugins.runtime

import java.nio.file.Path

/** Fetches a file to [target], failing with [java.io.IOException] unless its SHA-256 is [sha256]. */
fun interface Downloader {
    suspend fun download(url: String, sha256: String, target: Path)
}

package dev.chieppa.arc.tools

/** Hears how much of a download has arrived: [done] bytes of [total], which is null when the server does not say. */
fun interface DownloadProgress {
    fun bytes(done: Long, total: Long?)

    companion object {
        /** Progress no one follows. */
        val None = DownloadProgress { _, _ -> }
    }
}

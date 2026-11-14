package dev.chieppa.arc.tools

import dev.chieppa.arc.core.activity.ActivityKind
import dev.chieppa.arc.core.activity.ActivityLog
import dev.chieppa.arc.core.activity.ActivityOutcome
import kotlinx.coroutines.test.runTest
import java.io.IOException
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ReportingDownloaderTest {
    private val log = ActivityLog()
    private val url = "https://example.com/node.zip"

    @Test
    fun `a download is reported with its url and host`() = runTest {
        ReportingDownloader({ _, _, _, _ -> }, log).download(url, "00", Path.of("node.zip"))
        val entry = log.entries().single()
        assertEquals(ActivityKind.Download, entry.kind)
        assertEquals("GET $url", entry.title)
        assertEquals("example.com", entry.place)
        assertEquals(ActivityOutcome.Succeeded, entry.outcome)
    }

    @Test
    fun `a failed download is reported with why`() = runTest {
        val failing = ReportingDownloader({ _, _, _, _ -> throw IOException("Downloading $url failed: HTTP 404") }, log)
        assertFailsWith<IOException> { failing.download(url, "00", Path.of("node.zip")) }
        assertEquals("Downloading $url failed: HTTP 404", log.entries().single().error)
    }
}

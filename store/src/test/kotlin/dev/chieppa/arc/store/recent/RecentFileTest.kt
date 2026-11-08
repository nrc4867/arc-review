package dev.chieppa.arc.store.recent

import dev.chieppa.arc.core.recent.RecentSources
import java.nio.file.Files
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals

class RecentFileTest {
    private val file = Files.createTempDirectory("arc-recent").resolve("recent.json")

    @Test
    fun recentSourcesSurviveARoundTrip() {
        val recent = RecentSources().record("api#412", "Rotate refresh tokens", 100).record("web:main", null, 200)
        RecentFile(file).save(recent)
        assertEquals(recent, RecentFile(file).load())
    }

    @Test
    fun aMissingOrBrokenFileHoldsNone() {
        assertEquals(RecentSources(), RecentFile(file).load())
        file.writeText("{ not json")
        assertEquals(RecentSources(), RecentFile(file).load())
    }
}

package dev.chieppa.arc.core.recent

import kotlin.test.Test
import kotlin.test.assertEquals

class RecentSourcesTest {
    @Test
    fun aSourceRecordedAgainMovesToTheFrontKeepingItsTitle() {
        val recent = RecentSources()
            .record("api#1", "Rotate tokens", 10)
            .record("web#2", null, 20)
            .record("api#1", null, 30)
        assertEquals(listOf(RecentSource("api#1", "Rotate tokens", 30), RecentSource("web#2", null, 20)), recent.sources)
    }

    @Test
    fun itKeepsTheNewestUpToItsLimit() {
        val recent = (1..RecentSources.LIMIT + 3).fold(RecentSources()) { list, n -> list.record("api#$n", null, n.toLong()) }
        assertEquals(RecentSources.LIMIT, recent.sources.size)
        assertEquals("api#${RecentSources.LIMIT + 3}", recent.sources.first().ref)
    }

    @Test
    fun removingTakesOneOut() {
        val recent = RecentSources().record("a#1", null, 1).record("a#2", null, 2).remove("a#1")
        assertEquals(listOf("a#2"), recent.sources.map { it.ref })
    }
}

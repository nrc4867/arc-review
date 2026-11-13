package dev.chieppa.arc.tools

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SemVerTest {
    private fun v(text: String) = requireNotNull(SemVer.parse(text))

    @Test
    fun ordersAsSemverDoes() {
        val ordered = listOf("0.1.0-SNAPSHOT", "0.1.0", "0.2.0-alpha", "0.2.0-alpha.1", "0.2.0-alpha.beta", "0.2.0-beta.2", "0.2.0-beta.11", "0.2.0", "0.10.0", "1.0.0")
        assertEquals(ordered, ordered.shuffled(kotlin.random.Random(1)).sortedBy(::v))
    }

    @Test
    fun readsTagsAndRefusesOtherText() {
        assertEquals(SemVer(1, 2, 3, listOf("rc", "1")), v("v1.2.3-rc.1"))
        assertTrue(v("1.2.3-rc.1").prerelease)
        assertEquals("1.2.3-rc.1", v("1.2.3-rc.1").toString())
        assertNull(SemVer.parse("1.2"))
        assertNull(SemVer.parse("latest"))
    }
}

package dev.chieppa.arc.plugins.runtime

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class PluginToolsTest {
    @Test
    fun theBundledPinsCoverThisHost() {
        val node = assertNotNull(PluginTools.bundledPins().pin(PluginTools.NODE))
        assertEquals(64, node.sha256.length)
        assertEquals(26, PluginTools.major(node.version))
        assertEquals(24, PluginTools.major("v24.16.0"))
    }
}

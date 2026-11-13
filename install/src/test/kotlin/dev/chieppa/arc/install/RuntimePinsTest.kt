package dev.chieppa.arc.install

import dev.chieppa.arc.tools.ToolPins
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class RuntimePinsTest {
    @Test
    fun theRuntimeIsPinnedForEveryReleaseTarget() {
        val pins = ToolPins.resource(InstallConstants::class.java, "/" + InstallConstants.RUNTIME_PINS)
        for (host in listOf("windows-x64", "macos-aarch64")) {
            val pin = assertNotNull(pins.pin(InstallConstants.RUNTIME_TOOL, host), host)
            assertEquals(64, pin.sha256.length)
            assertTrue(pin.url.startsWith("https://cache-redirector.jetbrains.com/intellij-jbr/jbr-25."))
            assertNotNull(ReleaseTarget.of(host))
        }
    }
}

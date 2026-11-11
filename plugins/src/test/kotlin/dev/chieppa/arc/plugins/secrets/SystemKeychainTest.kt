package dev.chieppa.arc.plugins.secrets

import org.junit.jupiter.api.Tag
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * This system's real keychain, with one secret of a plugin id made up for the run, written, rewritten, read and
 * deleted again. Not part of build: run with `./gradlew :plugins:keychainTest` on Windows or macOS.
 */
@Tag("keychain")
class SystemKeychainTest {
    private val store = SecretStores.system()
    private val plugin = "arc-keychain-test-${UUID.randomUUID()}"

    @Test
    fun aSecretIsWrittenReadRewrittenAndDeleted() {
        assertTrue(store.lasting)
        try {
            assertNull(store.read(plugin, KEY))
            store.write(plugin, KEY, "first sécret")
            assertEquals("first sécret", store.read(plugin, KEY))
            store.write(plugin, KEY, "second")
            assertEquals("second", store.read(plugin, KEY))
        } finally {
            store.delete(plugin, KEY)
        }
        assertNull(store.read(plugin, KEY))
        store.delete(plugin, KEY)
    }

    private companion object {
        const val KEY = "token"
    }
}

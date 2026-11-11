package dev.chieppa.arc.plugins.runtime

import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals

class PermissionFlagsTest {
    private val sources = Path.of("plugins").toAbsolutePath()
    private val host = Path.of("runtime", "host-1").toAbsolutePath()

    @Test
    fun grantReadingTheGivenFoldersAndWorkersOnly() {
        assertEquals(
            listOf("--permission", "--allow-fs-read=$sources", "--allow-fs-read=$host", "--allow-worker").sorted(),
            PermissionFlags.of(listOf(host, sources, sources)).sorted(),
        )
        assertEquals("--permission", PermissionFlags.of(emptyList()).first())
    }
}

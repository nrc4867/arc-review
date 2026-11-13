package dev.chieppa.arc.install

import org.junit.jupiter.api.Assumptions.assumeTrue
import java.nio.file.Files
import kotlin.io.path.createTempDirectory
import kotlin.io.path.writeText
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** Writes and reads one shortcut in a temp folder through the shell; on Windows only. */
class WindowsShortcutTest {
    private val dir = createTempDirectory("arc-shortcut")

    @AfterTest
    fun cleanUp() {
        dir.toFile().deleteRecursively()
    }

    @Test
    fun aShortcutKeepsItsTargetAndAppId() {
        assumeTrue(System.getProperty("os.name").startsWith("Windows"))
        val target = dir.resolve("bin/javaw.exe").also { Files.createDirectories(it.parent); it.writeText("") }
        val link = dir.resolve("Arc.lnk")
        WindowsShortcut.write(ShortcutSpec(target, "\"@${dir.resolve("launch.args")}\"", dir, dir.resolve("Arc.ico"), "Arc", "dev.chieppa.arc"), link)

        assertEquals("dev.chieppa.arc", WindowsShortcut.appId(link))
        assertEquals(target.toString(), WindowsShortcut.target(link))

        val other = dir.resolve("Other.lnk")
        WindowsShortcut.write(ShortcutSpec(target, "", dir, dir.resolve("x.ico"), "Other", ""), other)
        assertNull(WindowsShortcut.appId(other)?.takeIf { it.isNotEmpty() })
    }
}

package dev.chieppa.arc.install

import dev.chieppa.arc.tools.ToolInstaller
import kotlinx.coroutines.test.runTest
import java.io.IOException
import java.nio.file.Files
import kotlin.io.path.createTempDirectory
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class InstallerTest {
    private val dir = createTempDirectory("arc-install")
    private val data = dir.resolve("data")
    private val layout = InstallLayout(data)
    private val test = TestInstall(dir)
    private val launcher = RecordingLauncher()
    private val installer = Installer(layout, ToolInstaller(layout.runtimes, test.downloader), launcher, test.host)
    private val updater = Updater(layout, installer)

    @AfterTest
    fun cleanUp() {
        dir.toFile().deleteRecursively()
    }

    @Test
    fun installsTheRuntimeTheJarItsIconsAndTheLauncher() = runTest {
        val steps = mutableListOf<String>()
        val listener = object : InstallListener {
            override fun started(step: InstallStep) { steps += "start $step" }
            override fun finished(step: InstallStep) { steps += "end $step" }
        }
        val installed = installer.install(test.jar("0.2.0", test.runtime("25.1")), listener)

        assertEquals(layout.jar("0.2.0"), installed.jar)
        assertTrue(Files.isRegularFile(installed.jar))
        assertEquals("javaw 25.1", installed.javaHome.resolve("bin/javaw.exe").readText())
        assertTrue(installed.javaHome.startsWith(layout.runtimes.resolve("jbr-25.1")))
        assertEquals("ico 0.2.0", layout.ico.readText())
        assertEquals("icns 0.2.0", layout.icns.readText())
        assertEquals(LaunchArgs.text(LaunchArgs.of(installed.jar)), layout.launchArgs.readText())
        assertEquals(InstallState(installed), InstallState.read(layout))
        assertEquals(listOf(installed.javaHome), launcher.written)
        assertEquals(InstallStep.entries.flatMap { listOf("start $it", "end $it") }, steps)
    }

    @Test
    fun aFailedDownloadInstallsNothing() = runTest {
        test.offline = true
        assertFailsWith<IOException> { installer.install(test.jar("0.2.0", test.runtime("25.1"))) }
        assertFalse(Files.exists(layout.launchArgs))
        assertNull(InstallState.read(layout).current)
        assertTrue(launcher.written.isEmpty())
    }

    @Test
    fun anUpdateStagesWithoutChangingWhatStartsUntilActivated() = runTest {
        val runtime = test.runtime("25.1")
        val first = installer.install(test.jar("0.2.0", runtime))
        val launchArgs = layout.launchArgs.readText()

        val staged = updater.stage(test.jar("0.3.0", runtime))
        assertEquals(launchArgs, layout.launchArgs.readText())
        assertEquals(first, InstallState.read(layout).current)

        updater.activate(staged)
        assertEquals(InstallState(staged, first), InstallState.read(layout))
        assertEquals(LaunchArgs.text(LaunchArgs.of(staged.jar)), layout.launchArgs.readText())
        assertEquals(1, test.fetched.size, "the same runtime is not downloaded again")
        assertEquals(1, launcher.written.size, "the launcher stays when the runtime does")
        assertTrue(launcher.pinsRefreshed.isEmpty())
    }

    @Test
    fun aNewRuntimeRewritesTheLauncherAndPinsAndOlderVersionsGo() = runTest {
        val v1 = installer.install(test.jar("0.2.0", test.runtime("25.1")))
        val v2 = updater.stage(test.jar("0.3.0", test.runtime("25.2"))).also { updater.activate(it) }
        assertEquals(listOf(v1.javaHome, v2.javaHome), launcher.written)
        assertEquals(listOf(v2.javaHome), launcher.pinsRefreshed)

        val v3 = updater.stage(test.jar("0.4.0", test.runtime("25.3"))).also { updater.activate(it) }
        assertEquals(InstallState(v3, v2), InstallState.read(layout))
        assertFalse(Files.exists(v1.jar))
        assertFalse(Files.exists(layout.runtimes.resolve("jbr-25.1")))
        assertTrue(Files.exists(v2.jar))
        assertTrue(Files.exists(v2.javaHome))
    }

    @Test
    fun anOlderOrTheSameJarIsNoUpdate() = runTest {
        val runtime = test.runtime("25.1")
        installer.install(test.jar("0.3.0", runtime))
        assertFailsWith<IOException> { updater.stage(test.jar("0.2.0", runtime)) }
        assertFailsWith<IOException> { updater.stage(test.jar("0.3.0", runtime, "again.jar")) }
    }

    @Test
    fun switchingBackSwapsTheVersions() = runTest {
        val runtime = test.runtime("25.1")
        val v1 = installer.install(test.jar("0.2.0", runtime))
        assertNull(updater.switchBack())
        val v2 = updater.stage(test.jar("0.3.0", runtime)).also { updater.activate(it) }
        assertEquals(v1, updater.switchBack())
        assertEquals(InstallState(v1, v2), InstallState.read(layout))
        assertEquals(LaunchArgs.text(LaunchArgs.of(v1.jar)), layout.launchArgs.readText())
    }

    @Test
    fun uninstallingKeepsThePersonsData() = runTest {
        installer.install(test.jar("0.2.0", test.runtime("25.1")))
        data.resolve("settings.properties").writeText("theme=dark")
        Files.createDirectories(data.resolve("runtimes/node-26.13.0"))

        assertEquals(emptyList(), Uninstaller(layout, launcher).uninstall())
        assertFalse(launcher.exists())
        assertFalse(Files.exists(layout.app))
        assertFalse(Files.exists(layout.runtimes.resolve("jbr-25.1")))
        assertTrue(Files.exists(data.resolve("runtimes/node-26.13.0")))
        assertEquals("theme=dark", data.resolve("settings.properties").readText())
    }
}

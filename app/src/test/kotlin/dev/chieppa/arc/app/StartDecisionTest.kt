package dev.chieppa.arc.app

import dev.chieppa.arc.install.InstallState
import dev.chieppa.arc.install.InstalledVersion
import dev.chieppa.arc.install.SelfLocation
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createTempDirectory
import kotlin.io.path.writeText
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class StartDecisionTest {
    private val dir = createTempDirectory("arc-start")
    private val runtime = dir.resolve("data/runtimes/jbr-25/jbr").also { Files.createDirectories(it) }
    private val installedJar = dir.resolve("data/app/arc-review-0.3.0.jar").also { Files.createDirectories(it.parent); it.writeText("0.3.0") }
    private val installed = InstallState(InstalledVersion("0.3.0", installedJar, runtime))
    private val otherJava = dir.resolve("corretto")

    @AfterTest
    fun cleanUp() {
        dir.toFile().deleteRecursively()
    }

    private fun jar(name: String, text: String): Path = dir.resolve(name).also { it.writeText(text) }

    private fun decide(
        self: SelfLocation,
        state: InstallState = installed,
        launcher: Boolean = true,
        supported: Boolean = true,
    ) = StartDecision.of(self, state, launcher, supported) { a, b -> Files.readString(a) == Files.readString(b) }

    @Test
    fun withoutAJarOrFromTheLauncherArcRuns() {
        assertEquals(StartDecision.Run, decide(SelfLocation(null, null, otherJava, launched = false)))
        assertEquals(StartDecision.Run, decide(SelfLocation(jar("a.jar", "0.4.0"), "0.4.0", otherJava, launched = true)))
        assertEquals(StartDecision.Run, decide(SelfLocation(jar("a.jar", "0.4.0"), "0.4.0", otherJava, launched = false), supported = false))
    }

    @Test
    fun theInstalledJarOnTheInstalledRuntimeRuns() {
        assertEquals(StartDecision.Run, decide(SelfLocation(installedJar, "0.3.0", runtime, launched = false)))
    }

    @Test
    fun aJarWithNothingInstalledInstalls() {
        assertEquals(StartDecision.Install(update = false), decide(SelfLocation(jar("a.jar", "0.3.0"), "0.3.0", otherJava, false), InstallState()))
    }

    @Test
    fun aNewerJarUpdatesAndAnOlderOneNeverReplacesTheInstall() {
        assertEquals(StartDecision.Install(update = true), decide(SelfLocation(jar("new.jar", "0.4.0"), "0.4.0", otherJava, false)))
        assertEquals(StartDecision.OpenInstalled, decide(SelfLocation(jar("old.jar", "0.2.0"), "0.2.0", otherJava, false)))
        assertEquals(StartDecision.Run, decide(SelfLocation(jar("old.jar", "0.2.0"), "0.2.0", otherJava, false), launcher = false))
    }

    @Test
    fun theSameVersionOpensTheInstalledArcUnlessItDiffersOrTheInstallIsBroken() {
        assertEquals(StartDecision.OpenInstalled, decide(SelfLocation(jar("same.jar", "0.3.0"), "0.3.0", otherJava, false)))
        assertEquals(StartDecision.OpenInstalled, decide(SelfLocation(installedJar, "0.3.0", otherJava, false)))
        assertEquals(StartDecision.Install(update = true), decide(SelfLocation(jar("rebuilt.jar", "0.3.0 rebuilt"), "0.3.0", otherJava, false)))
        assertEquals(StartDecision.Install(update = false), decide(SelfLocation(jar("same.jar", "0.3.0"), "0.3.0", otherJava, false), launcher = false))
    }
}

package dev.chieppa.arc.app

import dev.chieppa.arc.install.ArcJar
import dev.chieppa.arc.install.InstallLayout
import dev.chieppa.arc.install.InstallState
import dev.chieppa.arc.install.Installer
import dev.chieppa.arc.install.Launcher
import dev.chieppa.arc.install.ReleaseTarget
import dev.chieppa.arc.install.SelfLocation
import dev.chieppa.arc.install.Uninstaller
import dev.chieppa.arc.install.Updater
import dev.chieppa.arc.tools.HttpDownloader
import dev.chieppa.arc.tools.Sha256
import dev.chieppa.arc.tools.ToolInstaller
import java.nio.file.Files
import java.nio.file.Path

/** Arc's installation in the data folder [dataDir], as `main` and the command line reach it. */
class Installation(dataDir: Path) {
    val layout = InstallLayout(dataDir)
    val launcher: Launcher = Launcher.forHost(layout)
    val installer = Installer(layout, ToolInstaller(layout.runtimes, HttpDownloader), launcher)
    val updater = Updater(layout, installer)
    val uninstaller = Uninstaller(layout, launcher)

    fun state(): InstallState = InstallState.read(layout)

    /** What to do with this process at [self]. */
    fun decide(self: SelfLocation): StartDecision =
        StartDecision.of(self, state(), launcher.exists(), supported(self.jar), ::sameContents)

    /** Whether this process is the installed Arc's current version, which updates itself. */
    fun isInstalledSelf(self: SelfLocation): Boolean {
        val current = state().current ?: return false
        val jar = self.jar ?: return false
        return runCatching { Files.isSameFile(jar, current.jar) }.getOrDefault(false)
    }

    /** Whether Arc installs itself here: a release target with a runtime pinned in [jar]. */
    private fun supported(jar: Path?): Boolean =
        jar != null && ReleaseTarget.of() != null && runCatching { ArcJar(jar).runtimePin() }.isSuccess

    private fun sameContents(a: Path, b: Path): Boolean =
        runCatching { Files.size(a) == Files.size(b) && Sha256.of(a) == Sha256.of(b) }.getOrDefault(false)
}

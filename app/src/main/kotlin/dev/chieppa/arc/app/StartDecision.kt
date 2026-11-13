package dev.chieppa.arc.app

import dev.chieppa.arc.install.InstallState
import dev.chieppa.arc.install.SelfLocation
import dev.chieppa.arc.tools.SemVer
import java.nio.file.Files
import java.nio.file.Path

/** What `main` does with the window before anything else: start Arc here, install it first, or hand over to the installed one. */
sealed interface StartDecision {
    /** Start Arc in this process. */
    data object Run : StartDecision

    /** Show the install window; [update] when it replaces an older installed version. */
    data class Install(val update: Boolean) : StartDecision

    /** Start the installed Arc through its launcher and exit. */
    data object OpenInstalled : StartDecision

    companion object {
        /**
         * The decision for this process at [self] given what is installed ([state], whether the [launcher] is there)
         * and whether this machine is one Arc installs on ([supported]). A process with no jar (Gradle's run), one the
         * launcher started, or the installed jar on the installed runtime runs. A jar newer than the installed one, the
         * same version with other contents, or any jar while nothing is installed, installs; so does the same version
         * while the installation is broken. An older or the same jar opens the installed Arc; an older one never
         * replaces it, and runs as it is while the installation is broken.
         * [sameContents] tells whether two jars hold the same bytes.
         */
        fun of(
            self: SelfLocation,
            state: InstallState,
            launcher: Boolean,
            supported: Boolean,
            sameContents: (Path, Path) -> Boolean,
        ): StartDecision {
            val jar = self.jar ?: return Run
            val version = self.version?.let(SemVer::parse)
            if (self.launched || !supported || version == null) return Run
            val current = state.current ?: return Install(update = false)
            if (same(jar, current.jar) && same(self.javaHome, current.javaHome)) return Run
            val whole = launcher && Files.isRegularFile(current.jar) && Files.isDirectory(current.javaHome)
            val installed = SemVer.parse(current.version) ?: return Install(update = false)
            return when {
                version > installed -> Install(update = true)
                version < installed -> if (whole) OpenInstalled else Run
                !whole -> Install(update = false)
                same(jar, current.jar) || sameContents(jar, current.jar) -> OpenInstalled
                else -> Install(update = true)
            }
        }

        private fun same(a: Path, b: Path): Boolean =
            a.toAbsolutePath().normalize() == b.toAbsolutePath().normalize() || runCatching { Files.isSameFile(a, b) }.getOrDefault(false)
    }
}

package dev.chieppa.arc.install

import dev.chieppa.arc.tools.SemVer
import java.io.IOException
import java.nio.file.Path

/**
 * Updates an installed Arc from a downloaded jar through [installer]: [stage] readies a newer jar and its runtime
 * while the running version carries on, [activate] makes it the one the launcher starts. [switchBack] makes the
 * previous version current again.
 */
class Updater(private val layout: InstallLayout, private val installer: Installer) {
    /** What is installed now. */
    fun state(): InstallState = InstallState.read(layout)

    /** Stages [jar], refusing one no newer than the current version. Throws [IOException] when it cannot. */
    suspend fun stage(jar: Path, listener: InstallListener = InstallListener.None): InstalledVersion {
        val current = state().current?.version?.let(SemVer::parse)
        val offered = ArcJar(jar).version()?.let(SemVer::parse) ?: throw IOException("$jar carries no version")
        if (current != null && offered <= current) throw IOException("$jar is $offered, not newer than $current")
        return installer.stage(jar, listener)
    }

    /** Makes the staged [version] the one the launcher starts. */
    suspend fun activate(version: InstalledVersion) = installer.activate(version)

    /** Makes the previous version current again; null when there is none. */
    suspend fun switchBack(): InstalledVersion? {
        val previous = state().previous ?: return null
        installer.activate(previous)
        return previous
    }
}

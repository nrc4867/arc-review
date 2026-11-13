package dev.chieppa.arc.install

import dev.chieppa.arc.tools.HostPlatform
import dev.chieppa.arc.tools.Sha256
import dev.chieppa.arc.tools.ToolInstaller
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

/**
 * Installs an Arc jar into [layout]: [stage] puts the runtime the jar pins (through [runtimes]), a copy of the jar and
 * its icons in place without touching what starts; [activate] makes a staged version the one `launch.args` and the
 * [launcher] start, keeps the version before it, and deletes older jars and runtimes. Every file is written beside
 * its place and moved in, so an interrupted step leaves the installation as it was.
 */
class Installer(
    private val layout: InstallLayout,
    private val runtimes: ToolInstaller,
    private val launcher: Launcher,
    private val host: String = HostPlatform.current,
) {
    /** Stages [jar] and makes it current, writing the launcher whatever state it is in. */
    suspend fun install(jar: Path, listener: InstallListener = InstallListener.None): InstalledVersion =
        stage(jar, listener).also { activate(it, listener, writeLauncher = true) }

    /** Puts [jar]'s runtime, a copy of it and its icons in the data folder. Throws [IOException] when it cannot. */
    suspend fun stage(jar: Path, listener: InstallListener = InstallListener.None): InstalledVersion {
        val arc = ArcJar(jar)
        val version = withContext(Dispatchers.IO) { arc.version() } ?: throw IOException("$jar carries no version")
        val pin = withContext(Dispatchers.IO) { arc.runtimePin(host) }
        listener.started(InstallStep.Runtime)
        val java = runtimes.install(pin) { done, total -> listener.runtimeBytes(done, total) }
        listener.finished(InstallStep.Runtime)
        listener.started(InstallStep.Jar)
        val copy = layout.jar(version)
        withContext(Dispatchers.IO) {
            if (!holds(copy, jar)) AtomicFiles.copy(jar, copy)
            copyIcon(arc, InstallConstants.ICO_RESOURCE, layout.ico)
            copyIcon(arc, InstallConstants.ICNS_RESOURCE, layout.icns)
        }
        listener.finished(InstallStep.Jar)
        return InstalledVersion(version, copy.toAbsolutePath().normalize(), JavaPrograms.homeOf(java).toAbsolutePath().normalize())
    }

    /**
     * Makes the staged [version] current: rewrites `launch.args` and the state, and the launcher when [writeLauncher]
     * is set, it is missing or the runtime changed, with any pinned copies then.
     */
    suspend fun activate(version: InstalledVersion, listener: InstallListener = InstallListener.None, writeLauncher: Boolean = false) {
        withContext(Dispatchers.IO) {
            val state = InstallState.read(layout)
            val next = state.withCurrent(version)
            AtomicFiles.write(layout.launchArgs, LaunchArgs.text(LaunchArgs.of(version.jar)))
            listener.started(InstallStep.Launcher)
            val runtimeChanged = state.current != null && state.current.javaHome != version.javaHome
            if (writeLauncher || runtimeChanged || !launcher.exists()) launcher.write(version.javaHome, version.version)
            if (runtimeChanged) launcher.refreshPins(version.javaHome, version.version)
            next.write(layout)
            listener.finished(InstallStep.Launcher)
            deleteUnused(next)
        }
    }

    /** Deletes the copied jars and runtimes neither the current nor the previous version uses; one in use is left. */
    private fun deleteUnused(state: InstallState) {
        val kept = listOfNotNull(state.current, state.previous)
        list(layout.app).filter { isJar(it) && kept.none { version -> version.jar == it } }.forEach { runCatching { Files.delete(it) } }
        list(layout.runtimes).filter { isRuntime(it) && kept.none { version -> version.javaHome.startsWith(it) } }
            .forEach { runCatching { AtomicFiles.deleteTree(it) } }
    }

    private fun isJar(path: Path): Boolean = path.fileName.toString().let { it.startsWith(InstallConstants.JAR_PREFIX) && it.endsWith(InstallConstants.JAR_SUFFIX) }

    private fun isRuntime(path: Path): Boolean = path.fileName.toString().startsWith(InstallConstants.RUNTIME_TOOL + RUNTIME_SEPARATOR)

    private fun list(folder: Path): List<Path> =
        if (Files.isDirectory(folder)) Files.list(folder).use { it.toList() }.map { it.toAbsolutePath().normalize() } else emptyList()

    private fun copyIcon(arc: ArcJar, resource: String, target: Path) {
        val partial = target.resolveSibling(InstallConstants.PARTIAL_PREFIX + target.fileName)
        try {
            if (arc.copyResource(resource, partial)) AtomicFiles.copy(partial, target)
        } finally {
            Files.deleteIfExists(partial)
        }
    }

    /** Whether [copy] holds [jar] already, which keeps a copy a running Arc has open from being written again. */
    private fun holds(copy: Path, jar: Path): Boolean =
        Files.isRegularFile(copy) && (Files.isSameFile(jar, copy) || (Files.size(jar) == Files.size(copy) && Sha256.of(jar) == Sha256.of(copy)))

    private companion object {
        const val RUNTIME_SEPARATOR = "-"
    }
}

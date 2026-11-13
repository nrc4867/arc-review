package dev.chieppa.arc.install

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

/**
 * Arc on Windows: `Arc.lnk` in the Start menu, running the runtime's `javaw.exe` on `launch.args` with Arc's icon and
 * AppUserModelID, so the taskbar groups and pins Arc's windows under it. A taskbar pin is a copy of the shortcut, so
 * the ones carrying Arc's id are rewritten when the runtime changes, and removed with the launcher.
 */
class StartMenuLauncher(private val layout: InstallLayout, private val places: LauncherPlaces) : Launcher {
    private val shortcut: Path get() = places.startMenu.resolve(InstallConstants.APP_NAME + LNK)

    /** The shortcut a launcher for the runtime at [javaHome] holds. */
    fun spec(javaHome: Path): ShortcutSpec = ShortcutSpec(
        target = JavaPrograms.windowed(javaHome),
        arguments = quoted(LaunchArgs.reference(layout.launchArgs)),
        workingDirectory = layout.app,
        icon = layout.ico,
        description = InstallConstants.APP_NAME,
        appId = InstallConstants.APP_ID,
    )

    override fun write(javaHome: Path, version: String) {
        Files.createDirectories(places.startMenu)
        val partial = Files.createTempFile(places.startMenu, InstallConstants.PARTIAL_PREFIX, LNK)
        try {
            WindowsShortcut.write(spec(javaHome), partial)
            Files.move(partial, shortcut, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
        } finally {
            Files.deleteIfExists(partial)
        }
    }

    override fun refreshPins(javaHome: Path, version: String) {
        pins().forEach { WindowsShortcut.write(spec(javaHome), it) }
    }

    /** The taskbar pins that carry Arc's AppUserModelID. */
    private fun pins(): List<Path> {
        if (!Files.isDirectory(places.taskbarPins)) return emptyList()
        val links = Files.list(places.taskbarPins).use { files -> files.filter { it.fileName.toString().endsWith(LNK, ignoreCase = true) }.toList() }
        return links.filter { runCatching { WindowsShortcut.appId(it) }.getOrNull() == InstallConstants.APP_ID }
    }

    override fun exists(): Boolean = Files.isRegularFile(shortcut)

    override fun remove() {
        Files.deleteIfExists(shortcut)
        pins().forEach(Files::deleteIfExists)
    }

    override fun start() {
        ProcessBuilder(EXPLORER, shortcut.toString()).start()
    }

    private fun quoted(text: String) = "\"" + text + "\""

    private companion object {
        const val LNK = ".lnk"
        const val EXPLORER = "explorer.exe"
    }
}

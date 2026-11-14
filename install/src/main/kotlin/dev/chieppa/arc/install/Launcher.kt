package dev.chieppa.arc.install

import java.nio.file.Path

/** The double-clickable way into Arc on this system, which runs a runtime's java on `launch.args`. */
interface Launcher {
    /** Writes the launcher, or brings it up to date, to start the runtime at [javaHome] for [version]. */
    fun write(javaHome: Path, version: String)

    /** Points every pinned copy of the launcher, such as a taskbar pin, at the runtime at [javaHome]. */
    fun refreshPins(javaHome: Path, version: String) {}

    /** Whether the launcher is in place. */
    fun exists(): Boolean

    /** Whether the launcher in place is the one [write] makes for [javaHome] and [version]. */
    fun isCurrent(javaHome: Path, version: String): Boolean = exists()

    /** Takes the launcher away. */
    fun remove()

    /** Starts Arc through the launcher, as a double-click would. */
    fun start()

    companion object {
        /** The launcher for this system: the Start menu on Windows, an app in Applications on macOS, none elsewhere. */
        fun forHost(layout: InstallLayout, places: LauncherPlaces = LauncherPlaces.system(), os: String = System.getProperty(OS_NAME)): Launcher = when {
            os.startsWith(WINDOWS) -> StartMenuLauncher(layout, places)
            os.startsWith(MAC) -> MacAppBundle(layout, places)
            else -> NoLauncher
        }

        private const val OS_NAME = "os.name"
        private const val WINDOWS = "Windows"
        private const val MAC = "Mac"
    }
}

/** No launcher, where Arc makes none. */
object NoLauncher : Launcher {
    override fun write(javaHome: Path, version: String) = Unit

    override fun exists(): Boolean = false

    override fun remove() = Unit

    override fun start() = Unit
}

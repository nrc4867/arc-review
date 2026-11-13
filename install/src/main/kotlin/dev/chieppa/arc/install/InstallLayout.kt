package dev.chieppa.arc.install

import java.nio.file.Path

/** Where the installation lives in the data folder [dataDir]: the runtimes, the installed jars, `launch.args` and the icons. */
class InstallLayout(val dataDir: Path) {
    val runtimes: Path get() = dataDir.resolve(InstallConstants.RUNTIMES_FOLDER)
    val app: Path get() = dataDir.resolve(InstallConstants.APP_FOLDER)
    val launchArgs: Path get() = app.resolve(InstallConstants.LAUNCH_ARGS)
    val state: Path get() = app.resolve(InstallConstants.STATE_FILE)
    val icons: Path get() = app.resolve(InstallConstants.ICONS_FOLDER)
    val ico: Path get() = icons.resolve(InstallConstants.ICO)
    val icns: Path get() = icons.resolve(InstallConstants.ICNS)

    /** The installed copy of the jar of [version]. */
    fun jar(version: String): Path = app.resolve(InstallConstants.JAR_PREFIX + version + InstallConstants.JAR_SUFFIX)
}

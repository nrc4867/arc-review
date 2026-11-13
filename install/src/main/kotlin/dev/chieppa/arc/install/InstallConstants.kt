package dev.chieppa.arc.install

/** Names Arc's installation uses in the data folder, the launchers and the jar. */
object InstallConstants {
    /** The name people see on the launcher. */
    const val APP_NAME = "Arc"

    /** The macOS bundle id and the Windows AppUserModelID. */
    const val APP_ID = "dev.chieppa.arc"

    /** The folder in the data folder that holds the installed jars, `launch.args` and the icons. */
    const val APP_FOLDER = "app"
    const val RUNTIMES_FOLDER = "runtimes"
    const val ICONS_FOLDER = "icons"
    const val LAUNCH_ARGS = "launch.args"
    const val STATE_FILE = "install.properties"
    const val JAR_PREFIX = "arc-review-"
    const val JAR_SUFFIX = ".jar"
    const val ICO = "Arc.ico"
    const val ICNS = "Arc.icns"

    /** The runtime's name in `runtime.properties`, and the start of its folders under [RUNTIMES_FOLDER]. */
    const val RUNTIME_TOOL = "jbr"

    /** Where a jar carries its runtime pin and its icons. */
    const val RUNTIME_PINS = "dev/chieppa/arc/install/runtime.properties"
    const val ICO_RESOURCE = "stretch-app/icon.ico"
    const val ICNS_RESOURCE = "stretch-app/icon.icns"

    /** The JVM options every launch takes. */
    const val NATIVE_ACCESS = "--enable-native-access=ALL-UNNAMED"

    /** Set on a process its launcher started, so it never hands itself to the launcher again. */
    const val LAUNCHED_PROPERTY = "arc.launched"

    /** Where a work folder or file waits before it moves into place. */
    const val PARTIAL_PREFIX = ".partial-"
}

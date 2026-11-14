package dev.chieppa.arc.tools

/** The machine Arc runs on, named as pin files name hosts: `windows-x64`, `macos-aarch64`, `macos-x64`, `linux-x64`. */
object HostPlatform {
    private val os = System.getProperty("os.name").lowercase()
    private val arm = System.getProperty("os.arch").lowercase().let { it == "aarch64" || it == "arm64" }

    val isWindows: Boolean = os.startsWith("windows")

    val isMac: Boolean = os.startsWith("mac")

    val current: String = when {
        isWindows -> "windows"
        isMac -> "macos"
        else -> "linux"
    } + if (arm) "-aarch64" else "-x64"
}

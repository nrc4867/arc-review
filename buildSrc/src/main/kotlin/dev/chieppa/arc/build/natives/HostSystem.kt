package dev.chieppa.arc.build.natives

/** The machine the build runs on, named as zig names hosts and as Arc names native platforms. */
object HostSystem {
    private val os = System.getProperty("os.name").lowercase()
    private val arm = System.getProperty("os.arch").lowercase().let { it == "aarch64" || it == "arm64" }

    private val osName: String = when {
        os.startsWith("windows") -> "windows"
        os.startsWith("mac") -> "macos"
        else -> "linux"
    }

    /** The host as zig's downloads name it, such as `x86_64-windows`. */
    val zigHost: String = (if (arm) "aarch64" else "x86_64") + "-" + osName

    /** The host as Arc's native platforms name it, such as `windows-x64`; matches `NativePlatform` in `syntax`. */
    val platform: String = osName + "-" + (if (arm) "aarch64" else "x64")

    /** Whether the host is Windows. */
    val isWindows: Boolean = osName == "windows"
}

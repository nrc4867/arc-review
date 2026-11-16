package dev.chieppa.arc.syntax.natives

/** The platform names the bundled libraries are kept under: `windows-x64`, `macos-aarch64`, `macos-x64`. */
object NativePlatform {
    /** This machine's platform name. */
    val current: String = of(System.getProperty("os.name"), System.getProperty("os.arch"))

    fun of(osName: String, osArch: String): String {
        val os = osName.lowercase()
        val arch = osArch.lowercase()
        val cpu = if (arch == "aarch64" || arch == "arm64") "aarch64" else "x64"
        return when {
            os.startsWith("windows") -> "windows-$cpu"
            os.startsWith("mac") -> "macos-$cpu"
            else -> "linux-$cpu"
        }
    }
}

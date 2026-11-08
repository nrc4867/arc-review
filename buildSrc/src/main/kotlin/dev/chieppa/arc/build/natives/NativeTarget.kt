package dev.chieppa.arc.build.natives

import org.gradle.api.GradleException

/**
 * A platform the native libraries are built for: Arc's platform name, zig's target, the library file naming and the
 * flags that platform's C library needs beyond [NativeFlags.COMMON] (glibc hides POSIX names under plain C11).
 */
enum class NativeTarget(
    val platform: String,
    val zigTarget: String,
    private val prefix: String,
    private val suffix: String,
    val flags: List<String> = emptyList(),
) {
    WindowsX64("windows-x64", "x86_64-windows-gnu", "", ".dll"),
    MacosAarch64("macos-aarch64", "aarch64-macos", "lib", ".dylib"),
    MacosX64("macos-x64", "x86_64-macos", "lib", ".dylib"),
    LinuxX64("linux-x64", "x86_64-linux-gnu", "lib", ".so", listOf("-D_POSIX_C_SOURCE=200112L", "-D_DEFAULT_SOURCE")),
    ;

    /** The file name of library [name] on this platform, as `System.mapLibraryName` gives it there. */
    fun fileName(name: String): String = prefix + name + suffix

    companion object {
        /** Every target, selected by [ALL]. */
        const val ALL = "all"

        /**
         * The targets a `natives` property value names: [ALL], or platform names separated by commas. Without a
         * value, the host's platform, or none when the host is not a target.
         */
        fun select(value: String?): List<NativeTarget> = when {
            value.isNullOrBlank() -> entries.filter { it.platform == HostSystem.platform }
            value.trim() == ALL -> entries
            else -> value.split(',').map(String::trim).filter(String::isNotEmpty).map(::of)
        }

        /** The target with platform name [platform]. */
        fun of(platform: String): NativeTarget = entries.firstOrNull { it.platform == platform }
            ?: throw GradleException("Unknown native platform '$platform'; known: ${entries.joinToString { it.platform }}, or $ALL")
    }
}

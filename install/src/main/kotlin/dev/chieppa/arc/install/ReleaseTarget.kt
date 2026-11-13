package dev.chieppa.arc.install

import dev.chieppa.arc.tools.HostPlatform

/** The platforms Arc is released for, named as release jars name them: `arc-review-<target>-<version>.jar`. */
object ReleaseTarget {
    private val byHost = mapOf("windows-x64" to "windows-x64", "macos-aarch64" to "macos-arm64")

    /** This machine's target, or null when Arc publishes no jar for it. */
    fun of(host: String = HostPlatform.current): String? = byHost[host]

    /** The name of [target]'s jar for [version]. */
    fun jarName(target: String, version: String): String = InstallConstants.JAR_PREFIX + "$target-$version" + InstallConstants.JAR_SUFFIX
}

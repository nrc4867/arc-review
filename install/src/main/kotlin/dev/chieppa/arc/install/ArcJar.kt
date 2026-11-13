package dev.chieppa.arc.install

import dev.chieppa.arc.tools.HostPlatform
import dev.chieppa.arc.tools.ToolPin
import dev.chieppa.arc.tools.ToolPins
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.Properties
import java.util.jar.JarFile

/** What an Arc jar at [path] says about itself: its version, the runtime it pins and its icons. */
class ArcJar(val path: Path) {
    /** The manifest's `Implementation-Version`, or null when it has none. */
    fun version(): String? = JarFile(path.toFile()).use { it.manifest?.mainAttributes?.getValue(IMPLEMENTATION_VERSION) }

    /** The runtime this jar pins for [host]. Throws [IOException] when it pins none. */
    fun runtimePin(host: String = HostPlatform.current): ToolPin {
        val properties = Properties()
        JarFile(path.toFile()).use { jar ->
            val entry = jar.getJarEntry(InstallConstants.RUNTIME_PINS) ?: throw IOException("$path pins no runtime")
            jar.getInputStream(entry).use { properties.load(it) }
        }
        return ToolPins(properties).pin(InstallConstants.RUNTIME_TOOL, host) ?: throw IOException("$path pins no runtime for $host")
    }

    /** Copies the resource [name] to [target]; false when the jar does not hold it. */
    fun copyResource(name: String, target: Path): Boolean = JarFile(path.toFile()).use { jar ->
        val entry = jar.getJarEntry(name) ?: return false
        Files.createDirectories(target.parent)
        jar.getInputStream(entry).use { Files.copy(it, target, StandardCopyOption.REPLACE_EXISTING) }
        true
    }

    private companion object {
        const val IMPLEMENTATION_VERSION = "Implementation-Version"
    }
}

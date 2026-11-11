package dev.chieppa.arc.build.plugins

import dev.chieppa.arc.build.natives.HostSystem
import dev.chieppa.arc.build.natives.VerifiedDownload
import org.gradle.api.GradleException
import org.gradle.process.ExecOperations
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.Properties

/**
 * The tools the build bundles the plugin host with, as `tools.properties` pins them, kept under [root] and shared by
 * every checkout: esbuild for this host, and the npm packages unpacked under `node_modules`. Each is downloaded and
 * checked once and appears in place only once complete.
 */
class PluginToolsCache(private val root: File, private val pins: Properties, private val exec: ExecOperations) {
    /** The esbuild program for this host. */
    fun esbuild(): File {
        val host = HostSystem.platform
        val home = install(ESBUILD, host)
        return home.resolve(pin(ESBUILD, host, PROGRAM))
    }

    /** The `node_modules` folder holding [packages], each installed if needed. */
    fun nodeModules(packages: List<String>): File {
        val modules = root.resolve(NODE_MODULES)
        for (name in packages) {
            val target = modules.resolve(name)
            val version = pins.getProperty("$name.$VERSION") ?: throw GradleException("$name is not pinned")
            val marker = target.resolve(".$version")
            if (marker.isFile) continue
            val home = install(name, ANY)
            target.deleteRecursively()
            target.parentFile.mkdirs()
            home.resolve(PACKAGE).copyRecursively(target)
            marker.writeText(version)
        }
        return modules
    }

    private fun install(tool: String, host: String): File {
        val version = pins.getProperty("$tool.$VERSION") ?: throw GradleException("$tool is not pinned")
        val home = root.resolve("$tool-$version")
        if (home.resolve(DONE).isFile) return home
        root.mkdirs()
        val work = Files.createTempDirectory(root.toPath(), WORK_PREFIX).toFile()
        try {
            val url = pin(tool, host, URL)
            val archive = work.resolve(url.substringAfterLast('/'))
            VerifiedDownload.fetch(url, pin(tool, host, SHA256), archive.toPath())
            val unpacked = work.resolve(UNPACKED).also { it.mkdirs() }
            // Windows' own tar (bsdtar) takes drive letters, which another tar on the path may read as a remote host.
            val tar = System.getenv(SYSTEM_ROOT)?.let { File(it, WINDOWS_TAR) }?.takeIf { it.isFile }?.path ?: TAR
            exec.exec { commandLine(tar, "-xzf", archive.path, "-C", unpacked.path) }
            unpacked.resolve(DONE).writeText(version)
            home.deleteRecursively()
            Files.move(unpacked.toPath(), home.toPath(), StandardCopyOption.ATOMIC_MOVE)
        } finally {
            work.deleteRecursively()
        }
        return home
    }

    private fun pin(tool: String, host: String, key: String): String =
        pins.getProperty("$tool.$host.$key") ?: throw GradleException("No $tool $key is pinned for $host")

    private companion object {
        const val ESBUILD = "esbuild"
        const val ANY = "any"
        const val VERSION = "version"
        const val URL = "url"
        const val SHA256 = "sha256"
        const val PROGRAM = "program"
        const val PACKAGE = "package"
        const val NODE_MODULES = "node_modules"
        const val DONE = ".installed"
        const val WORK_PREFIX = ".install-"
        const val UNPACKED = "unpacked"
        const val TAR = "tar"
        const val SYSTEM_ROOT = "SystemRoot"
        const val WINDOWS_TAR = "System32/tar.exe"
    }
}

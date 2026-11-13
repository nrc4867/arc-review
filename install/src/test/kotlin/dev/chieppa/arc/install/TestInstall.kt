package dev.chieppa.arc.install

import dev.chieppa.arc.tools.Downloader
import dev.chieppa.arc.tools.Sha256
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.jar.Attributes
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream
import java.util.jar.Manifest
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/** Small Arc jars and runtime archives written by tests, and a downloader that serves them from disk. */
class TestInstall(private val dir: Path) {
    /** The host the test jars pin a runtime for. */
    val host = "windows-x64"

    /** Each url the downloader was asked for. */
    val fetched = mutableListOf<String>()
    private val archives = mutableMapOf<String, Path>()

    /** A downloader serving the runtimes [runtime] made; it fails while [offline]. */
    var offline = false
    val downloader = Downloader { url, _, target, progress ->
        if (offline) throw IOException("offline")
        fetched += url
        val archive = archives[url] ?: throw IOException("404 $url")
        progress.bytes(0, Files.size(archive))
        Files.copy(archive, target)
        progress.bytes(Files.size(archive), Files.size(archive))
    }

    /** A runtime archive for [version], served at its url, and the pin text naming it. */
    fun runtime(version: String): String {
        val archive = dir.resolve("jbr-$version.zip")
        ZipOutputStream(Files.newOutputStream(archive)).use { zip ->
            zip.putNextEntry(ZipEntry("jbr/bin/java.exe"))
            zip.write("java $version".toByteArray())
            zip.closeEntry()
            zip.putNextEntry(ZipEntry("jbr/bin/javaw.exe"))
            zip.write("javaw $version".toByteArray())
            zip.closeEntry()
        }
        val url = "https://example.test/jbr-$version.zip"
        archives[url] = archive
        return """
            jbr.version=$version
            jbr.$host.url=$url
            jbr.$host.sha256=${Sha256.of(archive)}
            jbr.$host.program=jbr/bin/java.exe
        """.trimIndent()
    }

    /** An Arc jar of [version] pinning the runtime [runtime] gave. */
    fun jar(version: String, runtime: String, name: String = "arc-$version.jar"): Path {
        val jar = dir.resolve(name)
        val manifest = Manifest().apply {
            mainAttributes[Attributes.Name.MANIFEST_VERSION] = "1.0"
            mainAttributes[Attributes.Name.IMPLEMENTATION_VERSION] = version
        }
        JarOutputStream(Files.newOutputStream(jar), manifest).use { out ->
            fun put(path: String, text: String) {
                out.putNextEntry(JarEntry(path))
                out.write(text.toByteArray())
                out.closeEntry()
            }
            put(InstallConstants.RUNTIME_PINS, runtime)
            put(InstallConstants.ICO_RESOURCE, "ico $version")
            put(InstallConstants.ICNS_RESOURCE, "icns $version")
        }
        return jar
    }
}

/** A launcher that keeps what it was asked to do. */
class RecordingLauncher : Launcher {
    val written = mutableListOf<Path>()
    val pinsRefreshed = mutableListOf<Path>()
    var starts = 0
    private var present = false

    override fun write(javaHome: Path, version: String) {
        written.add(javaHome)
        present = true
    }

    override fun refreshPins(javaHome: Path, version: String) {
        pinsRefreshed.add(javaHome)
    }

    override fun exists(): Boolean = present

    override fun remove() {
        present = false
    }

    override fun start() {
        starts++
    }
}

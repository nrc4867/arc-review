package dev.chieppa.arc.build.natives

import org.gradle.api.GradleException
import org.gradle.api.file.ArchiveOperations
import org.gradle.api.file.FileSystemOperations
import org.gradle.process.ExecOperations
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

/**
 * Puts the pinned zig for this host under a shared folder, downloading and verifying it the first time, and gives
 * its executable. An install appears in place only once it is complete, so an interrupted one is never used.
 */
class ZigInstaller(
    private val exec: ExecOperations,
    private val files: FileSystemOperations,
    private val archives: ArchiveOperations,
) {
    fun install(root: File): File {
        val download = ZigRelease.downloads[HostSystem.zigHost]
            ?: throw GradleException("No zig ${ZigRelease.VERSION} is pinned for host ${HostSystem.zigHost}")
        val home = root.resolve(download.folderName)
        val executable = home.resolve(if (HostSystem.isWindows) "zig.exe" else "zig")
        if (executable.isFile) return executable
        root.mkdirs()
        val work = Files.createTempDirectory(root.toPath(), UNPACK_PREFIX).toFile()
        try {
            val archive = work.resolve(download.fileName)
            VerifiedDownload.fetch(download.url, download.sha256, archive.toPath())
            val unpacked = work.resolve(UNPACKED).also { it.mkdirs() }
            unpack(download, archive, unpacked)
            val folder = unpacked.resolve(download.folderName)
            if (!folder.isDirectory) throw GradleException("${download.fileName} does not hold ${download.folderName}")
            if (!home.exists()) Files.move(folder.toPath(), home.toPath(), StandardCopyOption.ATOMIC_MOVE)
        } finally {
            files.delete { delete(work) }
        }
        return executable
    }

    // Windows' own tar (bsdtar) reads zips many times faster than Gradle's zipTree; zipTree is the fallback.
    private fun unpack(download: ZigDownload, archive: File, into: File) {
        val windowsTar = System.getenv(SYSTEM_ROOT)?.let { File(it, WINDOWS_TAR) }?.takeIf { it.isFile }
        when {
            !download.isZip -> exec.exec { commandLine("tar", "-xJf", archive.path, "-C", into.path) }
            windowsTar != null -> exec.exec { commandLine(windowsTar.path, "-xf", archive.path, "-C", into.path) }
            else -> files.copy { from(archives.zipTree(archive)); into(into) }
        }
    }

    private companion object {
        const val UNPACK_PREFIX = ".unpack-"
        const val UNPACKED = "unpacked"
        const val SYSTEM_ROOT = "SystemRoot"
        const val WINDOWS_TAR = "System32/tar.exe"
    }
}

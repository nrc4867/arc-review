package dev.chieppa.arc.build.natives

import org.gradle.api.GradleException
import org.gradle.api.file.FileSystemOperations
import org.gradle.process.ExecOperations
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

/**
 * Checks out each pinned repository at its commit under a shared folder, once: a checkout appears in place only when
 * complete and verified, and is reused after. Only git runs; nothing from the repositories is run.
 */
class GrammarCheckouts(private val exec: ExecOperations, private val files: FileSystemOperations) {
    /** Each pin's source folder, holding its C sources. */
    fun checkout(root: File, pins: List<GrammarPin>): Map<GrammarPin, File> {
        root.mkdirs()
        pins.distinctBy { it.checkoutName }.forEach { ensure(root, it) }
        return pins.associateWith { root.resolve(it.checkoutName).resolve(it.folder) }
    }

    private fun ensure(root: File, pin: GrammarPin) {
        val target = root.resolve(pin.checkoutName)
        if (target.isDirectory) return
        val work = Files.createTempDirectory(root.toPath(), FETCH_PREFIX).toFile()
        try {
            git(work, "init", "-q")
            git(work, "config", "core.autocrlf", "false")
            git(work, "fetch", "-q", "--depth", "1", pin.repository, pin.commit)
            git(work, "-c", "advice.detachedHead=false", "checkout", "-q", "FETCH_HEAD")
            val out = ByteArrayOutputStream()
            exec.exec {
                commandLine("git", "-C", work.path, "rev-parse", "HEAD")
                standardOutput = out
            }
            val head = out.toString().trim()
            if (head != pin.commit) throw GradleException("${pin.repository} checked out at $head, not the pinned ${pin.commit}")
            if (!target.exists()) Files.move(work.toPath(), target.toPath(), StandardCopyOption.ATOMIC_MOVE)
        } finally {
            if (work.exists()) files.delete { delete(work) }
        }
    }

    private fun git(dir: File, vararg args: String) {
        exec.exec { commandLine(listOf("git", "-C", dir.path) + args) }
    }

    private companion object {
        const val FETCH_PREFIX = ".fetch-"
    }
}

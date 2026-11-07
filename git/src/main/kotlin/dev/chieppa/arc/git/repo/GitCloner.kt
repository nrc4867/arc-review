package dev.chieppa.arc.git.repo

import dev.chieppa.arc.git.process.GitException
import dev.chieppa.arc.git.process.GitRunner
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.deleteRecursively
import kotlin.io.path.exists

/**
 * Makes the app's own clones: blobless and without a checkout, since Arc reads every file by its revision. A clone
 * that fails leaves no folder behind.
 */
class GitCloner(private val runner: GitRunner = GitRunner()) {
    /** Clones [url] into [target], which must not exist yet. Throws [GitException] when git cannot clone it. */
    @OptIn(kotlin.io.path.ExperimentalPathApi::class)
    suspend fun clone(url: String, target: Path) {
        val parent = target.toAbsolutePath().parent
        Files.createDirectories(parent)
        try {
            runner.run(parent, listOf("clone", "--quiet", "--no-checkout", "--filter=blob:none", url, target.toAbsolutePath().toString()))
        } catch (e: GitException) {
            if (target.exists()) target.deleteRecursively()
            throw e
        }
    }
}

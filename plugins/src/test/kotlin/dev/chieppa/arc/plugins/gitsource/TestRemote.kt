package dev.chieppa.arc.plugins.gitsource

import java.nio.file.Files
import java.nio.file.Path

/**
 * A git remote in [dir] for tests: a bare repo, with a working copy that commits plugin files and pushes them to it.
 * Nothing goes over the network.
 */
class TestRemote(private val dir: Path) {
    val bare: Path = dir.resolve("remote.git")
    private val work: Path = dir.resolve("work")

    /** The remote's url, as a person would paste it. */
    val url: String get() = bare.toUri().toString()

    init {
        Files.createDirectories(work)
        git(dir, "init", "--quiet", "--bare", "--initial-branch=main", bare.toString())
        git(work, "init", "--quiet", "--initial-branch=main")
        git(work, "remote", "add", "origin", bare.toString())
    }

    /** Commits [files] (path to text) on main, tagged [tag] when given, pushes, and gives the commit's sha. */
    fun commit(files: Map<String, String>, tag: String? = null): String {
        files.forEach { (path, text) ->
            val file = work.resolve(path)
            Files.createDirectories(file.parent)
            Files.writeString(file, text)
        }
        git(work, "add", "-A")
        git(work, "-c", "user.name=Arc", "-c", "user.email=arc@example.com", "commit", "--quiet", "-m", "plugin")
        tag?.let { git(work, "tag", it) }
        git(work, "push", "--quiet", "--tags", "origin", "main")
        return git(work, "rev-parse", "HEAD").trim()
    }

    /** A plugin's manifest at [version], declaring [capabilities] as JSON. */
    fun manifest(version: String, capabilities: String = "{}"): String =
        """{"id":"speckit","version":"$version","apiVersion":1,"entry":"src/index.tsx","capabilities":$capabilities}"""

    private fun git(at: Path, vararg args: String): String {
        val process = ProcessBuilder(listOf("git") + args).directory(at.toFile()).redirectErrorStream(true).start()
        val output = process.inputStream.readAllBytes().toString(Charsets.UTF_8)
        check(process.waitFor() == 0) { "git ${args.joinToString(" ")}: $output" }
        return output
    }
}

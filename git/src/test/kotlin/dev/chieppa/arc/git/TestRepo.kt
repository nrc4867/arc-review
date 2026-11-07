package dev.chieppa.arc.git

import dev.chieppa.arc.core.change.ObjectId
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.TimeUnit

/** A git repo in a temporary folder, built by tests through the git CLI with a fixed identity and no user config. */
class TestRepo(val dir: Path = Files.createTempDirectory("arc-git-test")) {
    init {
        git("init", "-q", "-b", "main")
        git("config", "user.email", "test@example.com")
        git("config", "user.name", "Test")
        git("config", "core.autocrlf", "false")
        git("config", "commit.gpgsign", "false")
    }

    /** Runs git in the repo and returns what it printed, failing the test when git fails. */
    fun git(vararg args: String): String = run(args.toList(), emptyMap())

    /** Runs git as [git] does, with commits it makes dated [time] (epoch seconds). */
    fun gitAt(time: Long, vararg args: String): String = run(args.toList(), mapOf("GIT_AUTHOR_DATE" to "@$time +0000", "GIT_COMMITTER_DATE" to "@$time +0000"))

    /** Writes [files], stages everything and commits it as [message], dated [time] (epoch seconds). */
    fun commitAt(time: Long, message: String, files: Map<String, String> = emptyMap()): ObjectId {
        files.forEach { (path, text) -> write(path, text) }
        git("add", "-A")
        gitAt(time, "commit", "-q", "--allow-empty", "-m", message)
        return head()
    }

    private fun run(args: List<String>, env: Map<String, String>): String {
        val process = ProcessBuilder(listOf("git") + args).directory(dir.toFile()).redirectErrorStream(true).apply {
            environment()["GIT_CONFIG_NOSYSTEM"] = "1"
            environment()["GIT_CONFIG_GLOBAL"] = dir.resolveSibling("${dir.fileName}.gitconfig").toString()
            environment().putAll(env)
        }.start()
        val out = process.inputStream.readAllBytes().toString(Charsets.UTF_8)
        check(process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS) && process.exitValue() == 0) { "git ${args.joinToString(" ")}: $out" }
        return out.trim()
    }

    fun write(path: String, text: String) {
        val file = dir.resolve(path)
        Files.createDirectories(file.parent)
        Files.writeString(file, text)
    }

    fun writeBytes(path: String, bytes: ByteArray) {
        val file = dir.resolve(path)
        Files.createDirectories(file.parent)
        Files.write(file, bytes)
    }

    /** Writes [files], stages everything and commits it as [message]. */
    fun commit(message: String, files: Map<String, String> = emptyMap()): ObjectId {
        files.forEach { (path, text) -> write(path, text) }
        git("add", "-A")
        git("commit", "-q", "--allow-empty", "-m", message)
        return head()
    }

    fun head(): ObjectId = ObjectId(git("rev-parse", "HEAD"))

    fun rev(name: String): ObjectId = ObjectId(git("rev-parse", name))

    private companion object {
        const val TIMEOUT_SECONDS = 60L
    }
}

/** [count] numbered lines, `line 1` to `line N`, each ending in a newline. */
fun lines(count: Int, change: (Int, String) -> String = { _, line -> line }): String =
    (1..count).joinToString("") { change(it, "line $it") + "\n" }

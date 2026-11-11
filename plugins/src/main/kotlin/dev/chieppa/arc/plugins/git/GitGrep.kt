package dev.chieppa.arc.plugins.git

import dev.chieppa.arc.plugins.command.CommandRunner
import dev.chieppa.arc.plugins.protocol.GrepHit
import java.nio.file.Path

/** Searches a repo's files at a revision with `git grep`, through [runner]. */
class GitGrep(private val runner: CommandRunner, private val git: String = GIT) {
    /** Lines matching [pattern] in [worktree]'s files at [revision] (its checkout when null), at most [limit]. */
    suspend fun grep(worktree: Path, pattern: String, revision: String?, limit: Int): List<GrepHit> {
        val command = listOf(git, "grep", "-n", "-I", "--no-color", "--full-name", "-E", "-e", pattern) + listOfNotNull(revision) + SEPARATOR
        val result = runner.run(command, worktree)
        if (result.code != 0) return emptyList()
        return parse(result.output, revision).take(limit)
    }

    /** Reads `path:line:text` lines, each led by `revision:` when one was searched. */
    fun parse(output: String, revision: String?): List<GrepHit> = output.lineSequence().mapNotNull { raw ->
        val line = revision?.let { raw.removePrefix("$it:") } ?: raw
        val match = HIT.find(line) ?: return@mapNotNull null
        GrepHit(match.groupValues[1], match.groupValues[2].toInt(), match.groupValues[3])
    }.toList()

    private companion object {
        const val GIT = "git"
        const val SEPARATOR = "--"
        val HIT = Regex("""^(.+?):(\d+):(.*)$""")
    }
}

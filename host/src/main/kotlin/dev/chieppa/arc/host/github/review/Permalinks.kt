package dev.chieppa.arc.host.github.review

import dev.chieppa.arc.host.github.repo.GitHubRepo

/** Links to lines of a file at one commit on github.com. */
object Permalinks {
    const val WEB = "https://github.com"

    /** Line [line] of [path] at [sha], or the lines from [startLine] to [line]. */
    fun line(repo: GitHubRepo, sha: String, path: String, line: Int, startLine: Int? = null): String {
        val lines = if (startLine != null && startLine != line) "L$startLine-L$line" else "L$line"
        return "$WEB/${repo.owner}/${repo.name}/blob/$sha/$path#$lines"
    }
}

package dev.chieppa.arc.host.github

import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.host.github.repo.GitHubRepo
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

/**
 * The github.com pages for what Arc shows, in [repo]'s repo on GitHub: a source's own page, and a file of a source's
 * change in a pull request's Files view or at one revision.
 */
object GitHubPages {
    private const val WEB = "https://github.com"
    private const val SHA_256 = "SHA-256"

    /**
     * [source]'s page: a pull request's, a commit's, the comparison a commit range or a branch against its base makes,
     * and a branch without a base its own page.
     */
    fun source(repo: GitHubRepo, source: Source): String = when (source) {
        is Source.PullRequest -> pull(repo, source.number)
        is Source.Commit -> "${home(repo)}/commit/${source.sha.value}"
        is Source.CommitRange -> "${home(repo)}/compare/${source.from.value}..${source.to.value}"
        is Source.BranchAgainstBase -> source.base?.let { "${home(repo)}/compare/${path(it.value)}...${path(source.branch.value)}" }
            ?: "${home(repo)}/tree/${path(source.branch.value)}"
    }

    fun pull(repo: GitHubRepo, number: Int): String = "${home(repo)}/pull/$number"

    /**
     * [path] in pull request [number]'s Files view, at [line] when given: on the change's new side, or its old side when
     * [old], as GitHub anchors a file by the SHA-256 of its path.
     */
    fun pullFile(repo: GitHubRepo, number: Int, path: String, line: Int? = null, old: Boolean = false): String {
        val side = if (old) OLD_SIDE else NEW_SIDE
        return "${pull(repo, number)}/files#diff-${sha256(path)}" + (line?.let { "$side$it" } ?: "")
    }

    /** [path] as it is at commit [sha], at [line] when given. */
    fun blob(repo: GitHubRepo, sha: String, path: String, line: Int? = null): String =
        "${home(repo)}/blob/$sha/${path(path)}" + (line?.let { "#L$it" } ?: "")

    private fun home(repo: GitHubRepo): String = "$WEB/${repo.owner}/${repo.name}"

    /** [text] for a URL path, each part between slashes encoded. */
    private fun path(text: String): String = text.split('/').joinToString("/") { URLEncoder.encode(it, StandardCharsets.UTF_8).replace("+", "%20") }

    private fun sha256(text: String): String =
        MessageDigest.getInstance(SHA_256).digest(text.toByteArray(StandardCharsets.UTF_8)).joinToString("") { "%02x".format(it) }

    private const val NEW_SIDE = "R"
    private const val OLD_SIDE = "L"
}

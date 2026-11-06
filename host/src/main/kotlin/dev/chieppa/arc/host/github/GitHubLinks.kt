package dev.chieppa.arc.host.github

import dev.chieppa.arc.core.source.BranchName
import dev.chieppa.arc.core.source.CommitSha
import dev.chieppa.arc.core.source.RepoName
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.host.github.GitHubLinkSyntax.BASE
import dev.chieppa.arc.host.github.GitHubLinkSyntax.COMMIT
import dev.chieppa.arc.host.github.GitHubLinkSyntax.COMMITS
import dev.chieppa.arc.host.github.GitHubLinkSyntax.COMPARE
import dev.chieppa.arc.host.github.GitHubLinkSyntax.DOMAIN
import dev.chieppa.arc.host.github.GitHubLinkSyntax.FORK_SEPARATOR
import dev.chieppa.arc.host.github.GitHubLinkSyntax.PULL
import dev.chieppa.arc.host.github.GitHubLinkSyntax.RANGE
import dev.chieppa.arc.host.github.GitHubLinkSyntax.SCHEMES
import dev.chieppa.arc.host.github.GitHubLinkSyntax.WWW_PREFIX
import java.net.URI
import java.net.URISyntaxException
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

/**
 * Reads a pasted github.com link as a source, the repo named `owner/name`:
 * - `/owner/name/pull/412`, with any tab after it such as `/files`: the pull request.
 * - `/owner/name/commit/<sha>` and `/owner/name/pull/412/commits/<sha>`: the commit.
 * - `/owner/name/compare/<base>...<head>`: the head branch against the base.
 * - `/owner/name/compare/<sha>..<sha>`: the commit range.
 *
 * A link without a scheme, such as `github.com/owner/name/pull/412`, reads the same. Queries and fragments are
 * ignored. Comparisons across forks (`base...fork:head`) name no source here.
 */
object GitHubLinks {
    fun parse(link: String): Source? {
        val uri = uri(link.trim()) ?: return null
        if (uri.scheme?.lowercase() !in SCHEMES) return null
        if (uri.host?.lowercase()?.removePrefix(WWW_PREFIX) != DOMAIN) return null
        val segments = uri.rawPath.orEmpty().split('/').filter { it.isNotEmpty() }.map(::decode)
        if (segments.size < 4) return null
        val repo = RepoName.parse("${segments[0]}/${segments[1]}") ?: return null
        val kind = segments[2]
        val rest = segments.drop(3)
        return when (kind) {
            PULL -> pullRequest(repo, rest)
            COMMIT -> CommitSha.parse(rest[0])?.let { Source.Commit(repo, it) }
            COMPARE -> compare(repo, rest.joinToString("/"))
            else -> null
        }
    }

    private fun uri(text: String): URI? {
        val withScheme = if ("://" in text) text else "https://$text"
        return try {
            URI(withScheme)
        } catch (_: URISyntaxException) {
            null
        }
    }

    private fun decode(segment: String): String = URLDecoder.decode(segment.replace("+", "%2B"), StandardCharsets.UTF_8)

    private fun pullRequest(repo: RepoName, rest: List<String>): Source? {
        val number = rest[0].takeIf { it.all(Char::isDigit) }?.toIntOrNull()?.takeIf { it > 0 } ?: return null
        if (rest.size >= 3 && rest[1] == COMMITS) return CommitSha.parse(rest[2])?.let { Source.Commit(repo, it) }
        return Source.PullRequest(repo, number)
    }

    private fun compare(repo: RepoName, spec: String): Source? {
        if (FORK_SEPARATOR in spec) return null
        if (BASE in spec) {
            val parts = spec.split(BASE)
            if (parts.size != 2) return null
            val base = BranchName.parse(parts[0]) ?: return null
            val head = BranchName.parse(parts[1]) ?: return null
            return Source.BranchAgainstBase(repo, head, base)
        }
        val parts = spec.split(RANGE)
        if (parts.size != 2) return null
        val from = CommitSha.parse(parts[0]) ?: return null
        val to = CommitSha.parse(parts[1]) ?: return null
        return Source.CommitRange(repo, from, to)
    }
}

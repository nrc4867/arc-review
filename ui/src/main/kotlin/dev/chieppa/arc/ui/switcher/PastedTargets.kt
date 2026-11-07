package dev.chieppa.arc.ui.switcher

import dev.chieppa.arc.core.source.RepoName
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.core.source.SourceRefs
import dev.chieppa.arc.host.github.GitHubLinks
import dev.chieppa.arc.host.github.repo.GitHubRepo

/** Reads the switcher's field as a github.com link or a source ref of a known repo, as the switcher shows them. */
object PastedTargets {
    /**
     * What [text] names: a github.com link, put in the clone [gitHubRepos] says comes from its repo, or a ref such as
     * `api#398` in one of [repos]. Null for anything else, which the switcher searches for.
     */
    fun read(text: String, gitHubRepos: Map<RepoName, GitHubRepo>, repos: Collection<RepoName>): PastedTarget? {
        val trimmed = text.trim()
        if (trimmed.isEmpty() || trimmed.any(Char::isWhitespace)) return null
        GitHubLinks.read(trimmed)?.let { link ->
            val remote = GitHubRepo.parse(link.source.repo.value)
            val local = gitHubRepos.entries.firstOrNull { it.value == remote }?.key
            return PastedTarget(
                named = link.source,
                source = local?.let { inRepo(link.source, it) },
                pullRequest = local?.let { repo -> link.pullRequest?.let { Source.PullRequest(repo, it) } },
                link = true,
            )
        }
        val source = SourceRefs.parse(trimmed)?.takeIf { it.repo in repos } ?: return null
        return PastedTarget(source, source, source as? Source.PullRequest, link = false)
    }

    /** [source] in [repo] instead of its own. */
    fun inRepo(source: Source, repo: RepoName): Source = when (source) {
        is Source.PullRequest -> source.copy(repo = repo)
        is Source.Commit -> source.copy(repo = repo)
        is Source.CommitRange -> source.copy(repo = repo)
        is Source.BranchAgainstBase -> source.copy(repo = repo)
    }
}

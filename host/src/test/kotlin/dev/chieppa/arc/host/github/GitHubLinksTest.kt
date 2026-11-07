package dev.chieppa.arc.host.github

import dev.chieppa.arc.core.source.BranchName
import dev.chieppa.arc.core.source.CommitSha
import dev.chieppa.arc.core.source.RepoName
import dev.chieppa.arc.core.source.Source
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GitHubLinksTest {
    private val repo = RepoName("acme/api")
    private val sha = "3fa9c1e0b6d24f1e9a7c5b3d2e1f0a9b8c7d6e5f"

    @Test
    fun aPullRequestLinkNamesThePullRequest() {
        assertEquals(Source.PullRequest(repo, 412), GitHubLinks.parse("https://github.com/acme/api/pull/412"))
    }

    @Test
    fun aPullRequestTabStillNamesThePullRequest() {
        listOf("/files", "/checks", "/files#diff-abc", "?w=1", "/").forEach { tail ->
            assertEquals(Source.PullRequest(repo, 412), GitHubLinks.parse("https://github.com/acme/api/pull/412$tail"), tail)
        }
    }

    @Test
    fun aCommitLinkNamesTheCommit() {
        assertEquals(Source.Commit(repo, CommitSha(sha)), GitHubLinks.parse("https://github.com/acme/api/commit/$sha"))
    }

    @Test
    fun aCommitInsideAPullRequestNamesTheCommit() {
        assertEquals(Source.Commit(repo, CommitSha(sha)), GitHubLinks.parse("https://github.com/acme/api/pull/412/commits/$sha"))
    }

    @Test
    fun aLinkSaysWhichPullRequestItSitsIn() {
        assertEquals(GitHubLink(Source.Commit(repo, CommitSha(sha)), 412), GitHubLinks.read("https://github.com/acme/api/pull/412/commits/$sha"))
        assertEquals(GitHubLink(Source.PullRequest(repo, 412), 412), GitHubLinks.read("https://github.com/acme/api/pull/412/files"))
        assertEquals(GitHubLink(Source.Commit(repo, CommitSha(sha))), GitHubLinks.read("https://github.com/acme/api/commit/$sha"))
    }

    @Test
    fun aComparisonOfBranchesNamesTheHeadAgainstTheBase() {
        assertEquals(
            Source.BranchAgainstBase(repo, BranchName("feature/login"), BranchName("main")),
            GitHubLinks.parse("https://github.com/acme/api/compare/main...feature/login"),
        )
    }

    @Test
    fun aComparisonOfCommitsNamesTheRange() {
        assertEquals(
            Source.CommitRange(repo, CommitSha("3fa9c1e"), CommitSha("9c1e2fa")),
            GitHubLinks.parse("https://github.com/acme/api/compare/3fa9c1e..9c1e2fa"),
        )
    }

    @Test
    fun aLinkWithoutSchemeOrWithWwwReadsTheSame() {
        assertEquals(Source.PullRequest(repo, 412), GitHubLinks.parse("github.com/acme/api/pull/412"))
        assertEquals(Source.PullRequest(repo, 412), GitHubLinks.parse("  https://www.github.com/acme/api/pull/412  "))
    }

    @Test
    fun anEncodedBranchNameIsDecoded() {
        assertEquals(
            Source.BranchAgainstBase(repo, BranchName("fix+retry"), BranchName("main")),
            GitHubLinks.parse("https://github.com/acme/api/compare/main...fix%2Bretry"),
        )
    }

    @Test
    fun linksThatNameNoSourceAreNull() {
        listOf(
            "",
            "not a link",
            "https://github.com/acme/api",
            "https://github.com/acme/api/issues/412",
            "https://github.com/acme/api/pull/abc",
            "https://github.com/acme/api/pull/0",
            "https://github.com/acme/api/commit/nothex",
            "https://github.com/acme/api/compare/main...other:feature",
            "https://github.com/acme/api/compare/main..feature",
            "https://gitlab.com/acme/api/pull/412",
            "https://github.com.example.org/acme/api/pull/412",
            "ftp://github.com/acme/api/pull/412",
        ).forEach { assertNull(GitHubLinks.parse(it), it) }
    }

    @Test
    fun theHostReadsLinksThroughTheSameRules() {
        assertEquals("github.com", GitHubHost.domain)
        assertEquals(Source.PullRequest(repo, 7), GitHubHost.sourceFromLink("https://github.com/acme/api/pull/7"))
    }
}

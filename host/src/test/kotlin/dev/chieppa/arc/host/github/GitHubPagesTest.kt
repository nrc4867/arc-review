package dev.chieppa.arc.host.github

import dev.chieppa.arc.core.source.BranchName
import dev.chieppa.arc.core.source.CommitSha
import dev.chieppa.arc.core.source.RepoName
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.host.github.repo.GitHubRepo
import kotlin.test.Test
import kotlin.test.assertEquals

class GitHubPagesTest {
    private val github = GitHubRepo("acme", "api")
    private val local = RepoName("api")
    private val sha = "3fa9c1e0b6d24f1e9a7c5b3d2e1f0a9b8c7d6e5f"
    private val other = "9c1e2fa0b6d24f1e9a7c5b3d2e1f0a9b8c7d6e5f"

    /** The SHA-256 of `src/app/Main.kt`, which GitHub's Files view anchors that file by. */
    private val mainAnchor = "diff-458d0fcaddf16f4bf1b0b5071951c9fda108407b4fccb41d46072e354ed949f9"

    @Test
    fun aPullRequestOpensItsPage() {
        assertEquals("https://github.com/acme/api/pull/412", GitHubPages.source(github, Source.PullRequest(local, 412)))
    }

    @Test
    fun aCommitOpensItsPage() {
        assertEquals("https://github.com/acme/api/commit/$sha", GitHubPages.source(github, Source.Commit(local, CommitSha(sha))))
    }

    @Test
    fun aRangeOpensItsComparisonWhichReadsBackAsTheRange() {
        val range = Source.CommitRange(local, CommitSha(sha), CommitSha(other))
        val url = GitHubPages.source(github, range)
        assertEquals("https://github.com/acme/api/compare/$sha..$other", url)
        assertEquals(Source.CommitRange(RepoName("acme/api"), CommitSha(sha), CommitSha(other)), GitHubLinks.parse(url))
    }

    @Test
    fun aBranchOpensItsComparisonWithItsBaseOrElseItsOwnPage() {
        val against = Source.BranchAgainstBase(local, BranchName("feature/login"), BranchName("main"))
        assertEquals("https://github.com/acme/api/compare/main...feature/login", GitHubPages.source(github, against))
        assertEquals("https://github.com/acme/api/tree/feature/login", GitHubPages.source(github, Source.BranchAgainstBase(local, BranchName("feature/login"))))
    }

    @Test
    fun aFileOfAPullRequestOpensInItsFilesViewAtTheLine() {
        assertEquals("https://github.com/acme/api/pull/412/files#$mainAnchor", GitHubPages.pullFile(github, 412, "src/app/Main.kt"))
        assertEquals("https://github.com/acme/api/pull/412/files#${mainAnchor}R20", GitHubPages.pullFile(github, 412, "src/app/Main.kt", 20))
        assertEquals("https://github.com/acme/api/pull/412/files#${mainAnchor}L7", GitHubPages.pullFile(github, 412, "src/app/Main.kt", 7, old = true))
    }

    @Test
    fun aFileAtARevisionOpensItsBlobAtTheLine() {
        assertEquals("https://github.com/acme/api/blob/$sha/src/app/Main.kt", GitHubPages.blob(github, sha, "src/app/Main.kt"))
        assertEquals("https://github.com/acme/api/blob/$sha/docs/A%20guide.md#L12", GitHubPages.blob(github, sha, "docs/A guide.md", 12))
    }
}

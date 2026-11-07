package dev.chieppa.arc.ui

import dev.chieppa.arc.host.github.pulls.CheckRollup
import dev.chieppa.arc.host.github.pulls.CheckState
import dev.chieppa.arc.host.github.pulls.PullLabel
import dev.chieppa.arc.host.github.pulls.PullRequestSummary
import dev.chieppa.arc.host.github.repo.GitHubRepo
import java.time.Instant

/** Pull requests as GitHub would answer them, for tests. */
object FakePulls {
    val api = GitHubRepo("lumen", "api")
    val auth = PullLabel("auth", "3987e5")
    val security = PullLabel("security", "e66767")
    private val created = Instant.parse("2026-11-01T10:00:00Z")

    fun pull(
        number: Int,
        title: String,
        labels: List<PullLabel> = listOf(auth),
        updated: Instant = created,
        checks: CheckState = CheckState.Passed,
        repo: GitHubRepo = api,
    ) = PullRequestSummary(repo, number, title, "https://github.com/${repo.fullName}/pull/$number", "jpark", false, created, updated, labels, comments = 3, checks = CheckRollup(checks))
}

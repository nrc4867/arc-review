package dev.chieppa.arc.host.github.repo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GitHubRemotesTest {
    private val arc = GitHubRepo("nrc4867", "arc-review")

    @Test
    fun httpsSshAndScpRemotesNameTheRepo() {
        assertEquals(arc, GitHubRemotes.parse("https://github.com/nrc4867/arc-review.git"))
        assertEquals(arc, GitHubRemotes.parse("https://github.com/nrc4867/arc-review"))
        assertEquals(arc, GitHubRemotes.parse("https://user@github.com/nrc4867/arc-review/"))
        assertEquals(arc, GitHubRemotes.parse("git@github.com:nrc4867/arc-review.git"))
        assertEquals(arc, GitHubRemotes.parse("ssh://git@github.com/nrc4867/arc-review.git"))
        assertEquals(arc, GitHubRemotes.parse("ssh://git@github.com:22/NRC4867/Arc-Review.git"))
    }

    @Test
    fun otherHostsAndPathsAreNotGitHubRepos() {
        assertNull(GitHubRemotes.parse("https://gitlab.com/nrc4867/arc-review.git"))
        assertNull(GitHubRemotes.parse("C:/repos/arc-review"))
        assertNull(GitHubRemotes.parse("https://github.com/nrc4867"))
    }
}

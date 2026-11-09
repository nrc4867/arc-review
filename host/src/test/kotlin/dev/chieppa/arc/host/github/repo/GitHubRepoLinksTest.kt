package dev.chieppa.arc.host.github.repo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GitHubRepoLinksTest {
    private val arc = GitHubRepo("nrc4867", "arc-review")

    @Test
    fun aRepoPageReadsWithOrWithoutItsScheme() {
        for (text in listOf("https://github.com/nrc4867/arc-review", "github.com/nrc4867/arc-review", "www.github.com/nrc4867/arc-review/", " https://github.com/nrc4867/arc-review.git ")) {
            assertEquals(RepoLink(arc, bare = false), GitHubRepoLinks.read(text), text)
        }
        assertEquals(RepoLink(arc, bare = false), GitHubRepoLinks.read("git@github.com:nrc4867/arc-review.git"))
    }

    @Test
    fun ownerAndNameReadAsBare() {
        assertEquals(RepoLink(arc, bare = true), GitHubRepoLinks.read("nrc4867/arc-review"))
    }

    @Test
    fun otherTextIsNoRepo() {
        assertNull(GitHubRepoLinks.read("https://github.com/nrc4867/arc-review/pull/3"))
        assertNull(GitHubRepoLinks.read("https://gitlab.com/nrc4867/arc-review"))
        assertNull(GitHubRepoLinks.read("token rotation"))
        assertNull(GitHubRepoLinks.read("spike/token/binding"))
        assertNull(GitHubRepoLinks.read("arc-review"))
    }
}

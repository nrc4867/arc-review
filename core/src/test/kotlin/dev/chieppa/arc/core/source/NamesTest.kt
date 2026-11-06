package dev.chieppa.arc.core.source

import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NamesTest {
    @Test
    fun branchNamesFollowGitRules() {
        listOf("main", "feature/login", "release-1.2", "hld/auth-v2", "a@b").forEach { assertTrue(BranchName.isValid(it), it) }
        listOf(
            "", "a..b", "a b", "a~1", "a^", "a:b", "a?", "a*", "a[", "a\\b", "-a", "/a", "a/", "a.", "a//b",
            ".a", "a/.b", "a.lock", "a/b.lock/c", "@", "a@{1}",
        ).forEach { assertFalse(BranchName.isValid(it), it) }
    }

    @Test
    fun repoNamesHaveAtMostAnOwner() {
        listOf("api", "acme/api", "my.repo", "a_b-c").forEach { assertTrue(RepoName.isValid(it), it) }
        listOf("", "a/b/c", "/a", "a/", "a b", "..", "a/..").forEach { assertFalse(RepoName.isValid(it), it) }
    }

    @Test
    fun shasAreLowercaseHexOfAUsableLength() {
        assertTrue(CommitSha.isValid("3fa9"))
        assertTrue(CommitSha.isValid("0123456789abcdef0123456789abcdef01234567"))
        assertFalse(CommitSha.isValid("3fa"))
        assertFalse(CommitSha.isValid("3FA9C1E"))
        assertFalse(CommitSha.isValid("3fa9g1e"))
    }

    @Test
    fun constructingAnInvalidValueFails() {
        assertFailsWith<IllegalArgumentException> { BranchName("a..b") }
        assertFailsWith<IllegalArgumentException> { RepoName("a/b/c") }
        assertFailsWith<IllegalArgumentException> { CommitSha("xyz") }
        assertFailsWith<IllegalArgumentException> { Source.PullRequest(RepoName("api"), 0) }
    }
}

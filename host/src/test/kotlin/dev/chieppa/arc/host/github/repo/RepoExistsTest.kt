package dev.chieppa.arc.host.github.repo

import dev.chieppa.arc.host.github.FakeGitHub
import dev.chieppa.arc.host.github.GitHub
import dev.chieppa.arc.host.github.RecordedTransport
import dev.chieppa.arc.host.github.api.ApiResponse
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RepoExistsTest {
    @Test
    fun aRepoExistsWhenGitHubFindsIt() = runTest {
        val transport = RecordedTransport(mutableListOf(ApiResponse(200, body = "{}"), ApiResponse(404, body = "{}")))
        val github = GitHub(FakeGitHub.client(transport))
        assertTrue(github.repoExists(GitHubRepo("nrc4867", "arc-review")))
        assertFalse(github.repoExists(GitHubRepo("nrc4867", "nope")))
        assertEquals("https://api.github.com/repos/nrc4867/arc-review", transport.requests.first().url)
    }
}

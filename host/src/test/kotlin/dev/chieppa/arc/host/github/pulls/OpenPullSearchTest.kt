package dev.chieppa.arc.host.github.pulls

import dev.chieppa.arc.host.github.FakeGitHub
import dev.chieppa.arc.host.github.GitHub
import dev.chieppa.arc.host.github.RecordedTransport
import dev.chieppa.arc.host.github.api.ApiResponse
import dev.chieppa.arc.host.github.repo.GitHubRepo
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class OpenPullSearchTest {
    private val api = GitHubRepo("lumen", "api")
    private val web = GitHubRepo("lumen", "web")

    @Test
    fun oneQueryNamesEveryRepo() {
        assertEquals("is:open in:title repo:lumen/api repo:lumen/web token", OpenPullSearch.query(listOf(api, web, GitHubRepo("Lumen", "API")), " token "))
    }

    @Test
    fun quotesCannotBreakTheQuery() {
        assertEquals("is:open in:title repo:lumen/api say  hi", OpenPullSearch.query(listOf(api), "say \"hi\""))
    }

    @Test
    fun noRepoOrNoTextSearchesNothing() = runTest {
        assertNull(OpenPullSearch.query(emptyList(), "token"))
        assertNull(OpenPullSearch.query(listOf(api), "  "))
        val transport = RecordedTransport(mutableListOf())
        assertEquals(emptyList(), GitHub(FakeGitHub.client(transport)).openPulls(listOf(api), " "))
        assertTrue(transport.requests.isEmpty())
    }

    @Test
    fun theSearchGoesInOneRequest() = runTest {
        val transport = RecordedTransport(mutableListOf(ApiResponse(200, body = FakeGitHub.resource("search.json"))))
        val pulls = GitHub(FakeGitHub.client(transport)).openPulls(listOf(api), "token")
        assertEquals(1, transport.requests.size)
        assertTrue("is:pr is:open in:title repo:lumen/api token" in transport.requests.single().body!!)
        assertEquals(listOf(14964, 14963, 14927), pulls.map { it.number })
    }
}

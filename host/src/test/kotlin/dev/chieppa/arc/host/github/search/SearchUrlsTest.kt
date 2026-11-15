package dev.chieppa.arc.host.github.search

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SearchUrlsTest {
    @Test
    fun thePersonsOwnListsGiveTheirQuery() {
        assertEquals(
            PastedSearch("is:open is:pr review-requested:@me", "review-requested:@me"),
            SearchUrls.parse("https://github.com/pulls?q=is%3Aopen+is%3Apr+review-requested%3A%40me"),
        )
        assertEquals("is:open author:@me", SearchUrls.parse("github.com/issues?q=is:open+author:@me")?.query)
    }

    @Test
    fun aSearchPageForPullRequestsOrIssuesGivesItsQuery() {
        val search = SearchUrls.parse("https://github.com/search?q=org%3Alumen+label%3Aauth+-is%3Adraft&type=pullrequests")
        assertEquals(PastedSearch("org:lumen label:auth -is:draft", "org:lumen label:auth"), search)
        assertEquals("label:bug", SearchUrls.parse("https://github.com/search?type=issues&q=label%3Abug")?.query)
        assertNull(SearchUrls.parse("https://github.com/search?q=arc&type=repositories"))
    }

    @Test
    fun aReposListAddsTheRepoAndIsNamedForIt() {
        assertEquals(
            PastedSearch("is:pr is:open repo:lumen/web", "lumen/web"),
            SearchUrls.parse("https://github.com/lumen/web/pulls?q=is%3Apr+is%3Aopen"),
        )
        assertEquals("repo:lumen/api label:x", SearchUrls.parse("https://github.com/lumen/web/pulls?q=repo%3Alumen%2Fapi+label%3Ax")?.query)
    }

    @Test
    fun anythingElseIsNoSearch() {
        assertNull(SearchUrls.parse("https://github.com/lumen/api/pull/412"))
        assertNull(SearchUrls.parse("https://gitlab.com/pulls?q=is:open"))
        assertNull(SearchUrls.parse("not a link"))
    }

    @Test
    fun aNameLeavesOutQualifiersAndIsCutShort() {
        assertNull(SearchUrls.nameOf("is:open is:pr sort:updated-desc"))
        val long = SearchUrls.nameOf("label:" + "x".repeat(60))!!
        assertEquals(SearchUrls.NAME_MAX, long.length)
    }

    @Test
    fun theShapesGitHubGivesAllRead() {
        val requested = "is:open is:pr review-requested:@me"
        for (link in listOf(
            "https://github.com/pulls?q=is%3Aopen+is%3Apr+review-requested%3A%40me",
            "https://github.com/pulls?q=is%3Aopen%20is%3Apr%20review-requested%3A%40me",
            "https://github.com/pulls?q=is:open+is:pr+review-requested:@me",
            "https://github.com/pulls/?q=is%3Aopen+is%3Apr+review-requested%3A%40me",
            "https://github.com/pulls?q=is%3Aopen+is%3Apr+review-requested%3A%40me&s=updated&o=desc",
            "https://github.com/search?q=is%3Aopen+is%3Apr+review-requested%3A%40me&type=pullrequests&s=updated&o=desc",
            "https://github.com/search?type=PullRequests&q=is%3Aopen+is%3Apr+review-requested%3A%40me",
            "https://www.github.com/pulls?q=is%3Aopen+is%3Apr+review-requested%3A%40me#top",
            "  github.com/pulls?q=is:open is:pr review-requested:@me \n",
            "https://github.com/pulls?q=is%3Aopen++is%3Apr+review-requested%3A%40me",
        )) {
            assertEquals(PastedSearch(requested, "review-requested:@me"), SearchUrls.parse(link), link)
        }
        assertEquals(
            "is:pr is:open label:\"needs review\" repo:Lumen/Web",
            SearchUrls.parse("https://github.com/Lumen/Web/pulls?q=is%3Apr+is%3Aopen+label%3A%22needs+review%22&s=updated")?.query,
        )
    }

    @Test
    fun listTabsWithoutAQuerySearchWhatTheyShow() {
        assertEquals("is:open is:pr review-requested:@me", SearchUrls.parse("https://github.com/pulls/review-requested")?.query)
        assertEquals("is:open is:pr assignee:@me", SearchUrls.parse("https://github.com/pulls/assigned/")?.query)
        assertEquals("is:open is:pr author:@me", SearchUrls.parse("https://github.com/pulls")?.query)
        assertEquals("is:open is:pr repo:lumen/web", SearchUrls.parse("https://github.com/lumen/web/pulls")?.query)
        assertEquals("is:issue state:open", SearchUrls.parse("https://github.com/issues/assigned?q=is%3Aissue+state%3Aopen")?.query)
    }

    @Test
    fun aRefusedLinkSaysWhy() {
        assertEquals(SearchReading.Refused(SearchProblem.NotGitHub), SearchUrls.read("https://gitlab.com/pulls?q=is:open"))
        assertEquals(SearchReading.Refused(SearchProblem.NotASearch), SearchUrls.read("https://github.com/lumen/api/pull/412"))
        assertEquals(SearchReading.Refused(SearchProblem.NoQuery), SearchUrls.read("https://github.com/search?type=pullrequests"))
        assertEquals(SearchReading.Refused(SearchProblem.NotPullRequests, "repositories"), SearchUrls.read("https://github.com/search?q=arc&type=repositories"))
        assertEquals(SearchReading.Refused(SearchProblem.UnclosedQuote), SearchUrls.read("https://github.com/pulls?q=label%3A%22oops"))
    }
}

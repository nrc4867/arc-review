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
        assertNull(SearchUrls.parse("https://github.com/pulls"))
        assertNull(SearchUrls.parse("not a link"))
    }

    @Test
    fun aNameLeavesOutQualifiersAndIsCutShort() {
        assertNull(SearchUrls.nameOf("is:open is:pr sort:updated-desc"))
        val long = SearchUrls.nameOf("label:" + "x".repeat(60))!!
        assertEquals(SearchUrls.NAME_MAX, long.length)
    }
}

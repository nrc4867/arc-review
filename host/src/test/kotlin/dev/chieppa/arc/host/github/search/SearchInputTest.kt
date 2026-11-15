package dev.chieppa.arc.host.github.search

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SearchInputTest {
    @Test
    fun aTypedQueryIsSavedAsTypedWithItsSpacesTidied() {
        assertEquals(
            SearchReading.Found(PastedSearch("is:pr is:open review-requested:@me org:foo", "review-requested:@me org:foo")),
            SearchInput.read("  is:pr  is:open review-requested:@me\torg:foo "),
        )
    }

    @Test
    fun aLinkIsReadAsALink() {
        assertEquals("is:open is:pr review-requested:@me", (SearchInput.read("https://github.com/pulls?q=is%3Aopen+is%3Apr+review-requested%3A%40me") as SearchReading.Found).search.query)
        assertEquals(SearchReading.Refused(SearchProblem.NotGitHub), SearchInput.read("gitlab.com/x/y/merge_requests"))
    }

    @Test
    fun queriesWithDotsOrSlashesAreNoLinks() {
        assertFalse(SearchInput.isLink("repo:lumen/web is:open"))
        assertFalse(SearchInput.isLink("v1.2 readme.md"))
        assertTrue(SearchInput.isLink("github.com/pulls"))
        assertTrue(SearchInput.isLink("HTTPS://github.com/pulls"))
    }

    @Test
    fun aBadQuerySaysWhy() {
        assertEquals(SearchReading.Refused(SearchProblem.Empty), SearchInput.read("   "))
        assertEquals(SearchReading.Refused(SearchProblem.EmptyQualifier, "label:"), SearchInput.read("is:pr label:"))
    }
}

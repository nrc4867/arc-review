package dev.chieppa.arc.core.source

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SourceRefsTest {
    private val api = RepoName("api")
    private val web = RepoName("web")

    @Test
    fun aHashNamesAPullRequest() {
        assertEquals(Source.PullRequest(api, 412), SourceRefs.parse("api#412"))
    }

    @Test
    fun anAtNamesACommit() {
        assertEquals(Source.Commit(web, CommitSha("3fa9c1e")), SourceRefs.parse("web@3fa9c1e"))
    }

    @Test
    fun aCommitShaIsReadInLowercase() {
        assertEquals(Source.Commit(web, CommitSha("3fa9c1e")), SourceRefs.parse("web@3FA9C1E"))
    }

    @Test
    fun twoDotsBetweenShasNameARange() {
        assertEquals(
            Source.CommitRange(web, CommitSha("3fa9c1e"), CommitSha("9c1e2fa")),
            SourceRefs.parse("web@3fa9c1e..9c1e2fa"),
        )
    }

    @Test
    fun threeDotsBetweenBranchesNameABranchAgainstABase() {
        assertEquals(
            Source.BranchAgainstBase(web, BranchName("feature"), BranchName("main")),
            SourceRefs.parse("web:feature...main"),
        )
    }

    @Test
    fun aBranchAloneIsComparedWithTheDefaultBranch() {
        assertEquals(
            Source.BranchAgainstBase(RepoName("platform-docs"), BranchName("hld/auth-v2")),
            SourceRefs.parse("platform-docs:hld/auth-v2"),
        )
    }

    @Test
    fun aRepoMayBeNamedWithItsOwner() {
        assertEquals(Source.PullRequest(RepoName("acme/api"), 7), SourceRefs.parse("acme/api#7"))
    }

    @Test
    fun surroundingSpaceIsIgnored() {
        assertEquals(Source.PullRequest(api, 412), SourceRefs.parse("  api#412\n"))
    }

    @Test
    fun theFirstMarkDecidesTheKind() {
        assertEquals(Source.BranchAgainstBase(web, BranchName("fix#12")), SourceRefs.parse("web:fix#12"))
        assertEquals(Source.BranchAgainstBase(web, BranchName("fix@home")), SourceRefs.parse("web:fix@home"))
    }

    @Test
    fun textThatNamesNoSourceIsNull() {
        listOf(
            "", "api", "#412", "api#", "api#0", "api#-3", "api#4x", "api#99999999999",
            "web@", "web@xyz123", "web@3fa", "web@3fa9c1e...9c1e2fa", "web@a1b2..", "web@a1b2..c3d4..e5f6",
            "web:", "web:feature...", "web:...main", "web:feature..main", "web:a...b...c", "web:has space",
            "a/b/c#1", "bad repo#1", "../x#1",
        ).forEach { assertNull(SourceRefs.parse(it), it) }
    }

    @Test
    fun formattingReadsBackToTheSameSource() {
        listOf("api#412", "web@3fa9c1e", "web@3fa9c1e..9c1e2fa", "web:feature...main", "platform-docs:hld/auth-v2", "acme/api#7")
            .forEach { ref ->
                val source = SourceRefs.parse(ref)!!
                assertEquals(ref, source.ref)
                assertEquals(source, SourceRefs.parse(source.ref))
            }
    }
}

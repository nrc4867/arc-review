package dev.chieppa.arc.ui

import dev.chieppa.arc.ui.switcher.CommitQuery
import dev.chieppa.arc.core.change.ObjectId
import dev.chieppa.arc.core.change.CommitInfo
import dev.chieppa.arc.core.change.ChangeStatus
import dev.chieppa.arc.core.change.ChangedFile
import dev.chieppa.arc.core.source.BranchName
import dev.chieppa.arc.core.source.CommitSha
import dev.chieppa.arc.core.source.RepoName
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.git.repo.LocalBranch
import dev.chieppa.arc.host.github.repo.GitHubRepo
import dev.chieppa.arc.core.recent.RecentSource
import dev.chieppa.arc.host.github.pulls.ReviewRequest
import dev.chieppa.arc.host.github.pulls.Viewer
import dev.chieppa.arc.ui.util.TypedPath
import dev.chieppa.arc.ui.switcher.FollowedLookup
import dev.chieppa.arc.ui.switcher.FollowedResults
import dev.chieppa.arc.ui.switcher.OpenAs
import dev.chieppa.arc.ui.switcher.PastedTargets
import dev.chieppa.arc.ui.switcher.SectionKind
import dev.chieppa.arc.ui.switcher.SwitcherInputs
import dev.chieppa.arc.ui.switcher.SwitcherItem
import dev.chieppa.arc.ui.switcher.SwitcherLimits
import dev.chieppa.arc.ui.switcher.SwitcherScope
import dev.chieppa.arc.ui.switcher.SwitcherSearch
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import java.nio.file.Path
import java.time.Instant

class SwitcherLogicTest {
    private val api = RepoName("api")
    private val web = RepoName("web")
    private val api412 = Source.PullRequest(api, 412)
    private val web88 = Source.PullRequest(web, 88)
    private val origins = mapOf(api to GitHubRepo("lumen", "api"), web to GitHubRepo("lumen", "web"))

    private val inputs = SwitcherInputs(
        sources = listOf(
            SwitcherItem.InWorkspace(api412, "Rotate refresh tokens on every use"),
            SwitcherItem.InWorkspace(web88, "Silent token refresh in useSession"),
        ),
        files = listOf(
            SwitcherItem.Changed(api412, ChangedFile("auth/token/TokenService.kt", ChangeStatus.Modified, added = 48, removed = 19)),
            SwitcherItem.Changed(api412, ChangedFile("auth/Login.java", ChangeStatus.Modified)),
            SwitcherItem.Changed(web88, ChangedFile("src/hooks/useTokenRefresh.test.tsx", ChangeStatus.Modified)),
        ),
        headFiles = listOf(SwitcherItem.AtHead(api412, "tokens/README.md")),
    )

    @Test
    fun everyScopeShowsSectionsInOrderWithTheirCounts() {
        val sections = SwitcherSearch.sections("tok", SwitcherScope.All, inputs)
        assertEquals(listOf(SectionKind.InWorkspace, SectionKind.Files), sections.map { it.kind })
        assertEquals(2, sections[0].total)
        val files = sections[1].items.map { (it as? SwitcherItem.Changed)?.file?.path ?: (it as SwitcherItem.AtHead).path }
        assertEquals(listOf("auth/token/TokenService.kt", "src/hooks/useTokenRefresh.test.tsx", "tokens/README.md"), files)
    }

    @Test
    fun sourcesMatchOnTheirRefToo() {
        val sections = SwitcherSearch.sections("web#", SwitcherScope.Sources, inputs)
        assertEquals(listOf(web88), sections.single().items.map { (it as SwitcherItem.InWorkspace).source })
    }

    @Test
    fun oneScopeShowsOnlyItsSections() {
        assertEquals(listOf(SectionKind.Files), SwitcherSearch.sections("", SwitcherScope.Files, inputs).map { it.kind })
        assertEquals(listOf(SectionKind.InWorkspace), SwitcherSearch.sections("", SwitcherScope.Sources, inputs).map { it.kind })
    }

    @Test
    fun anEmptySearchLeavesFilesAtHeadOutUntilFilesIsTheScope() {
        val all = SwitcherSearch.sections("", SwitcherScope.All, inputs).single { it.kind == SectionKind.Files }
        assertTrue(all.items.none { it is SwitcherItem.AtHead })
        val files = SwitcherSearch.sections("", SwitcherScope.Files, inputs).single()
        assertTrue(files.items.any { it is SwitcherItem.AtHead })
    }

    @Test
    fun tabCyclesThroughTheScopes() {
        assertEquals(SwitcherScope.Sources, SwitcherScope.All.next())
        assertEquals(SwitcherScope.All, SwitcherScope.Files.next())
        assertEquals(SwitcherScope.Files, SwitcherScope.All.next(back = true))
    }

    @Test
    fun followedReposLeaveOutWhatTheReviewHolds() {
        val pulls = listOf(FakePulls.pull(398, "Remove legacy token table"), FakePulls.pull(412, "Rotate refresh tokens"), FakePulls.pull(5, "Elsewhere", repo = GitHubRepo("other", "repo")))
        val branches = mapOf(api to listOf(LocalBranch("spike/token-binding", "mfischer", 0), LocalBranch("main", "x", 0), LocalBranch("held", "x", 0)))
        val held = listOf(api412, Source.BranchAgainstBase(api, BranchName("held")))
        val found = FollowedResults.of(pulls, branches, origins, held)
        assertEquals(
            listOf(Source.PullRequest(api, 398), Source.BranchAgainstBase(api, BranchName("spike/token-binding")), Source.BranchAgainstBase(api, BranchName("main"))),
            found.map(::sourceOf),
        )
    }

    @Test
    fun suggestionsPutWhatWaitsOnThePersonFirstThenWhatWasOpenedLately() {
        val older = FakePulls.pull(398, "Remove legacy token table", updated = Instant.parse("2026-11-01T00:00:00Z"))
        val newer = FakePulls.pull(401, "Paginate audit log", updated = Instant.parse("2026-11-07T00:00:00Z"))
        val waiting = FakePulls.pull(377, "Bump the driver", updated = Instant.parse("2026-10-01T00:00:00Z")).copy(requests = listOf(ReviewRequest.User("me")))
        val branches = mapOf(api to listOf(LocalBranch("spike/token-binding", "mfischer", 0)))
        val recent = listOf(RecentSource(Source.BranchAgainstBase(api, BranchName("spike/token-binding")).ref, "Spike", 10), RecentSource(Source.Commit(api, CommitSha("b7e19d2")).ref, "Old commit", 5), RecentSource("gone#1", null, 1))
        val found = FollowedResults.of(listOf(older, newer, waiting), branches, origins, emptyList(), recent, Viewer("me"))
        assertEquals(
            listOf(
                Source.PullRequest(api, 377),
                Source.BranchAgainstBase(api, BranchName("spike/token-binding")),
                Source.Commit(api, CommitSha("b7e19d2")),
                Source.PullRequest(api, 401),
                Source.PullRequest(api, 398),
            ),
            found.map(::sourceOf),
        )
        val shown = SwitcherSearch.sections("", SwitcherScope.Sources, SwitcherInputs(followed = found)).single()
        assertEquals(found, shown.items)
    }

    @Test
    fun followedSourcesMatchFuzzily() {
        val pulls = listOf(
            FakePulls.pull(398, "Remove legacy token table").copy(author = "mfischer"),
            FakePulls.pull(401, "Paginate audit log").copy(headBranch = "feature/audit-pages"),
        )
        val found = FollowedResults.of(pulls, emptyMap(), origins, emptyList())
        fun first(query: String) = sourceOf(SwitcherSearch.sections(query, SwitcherScope.Sources, SwitcherInputs(followed = found)).single().items.first())
        assertEquals(Source.PullRequest(api, 398), first("legcy tokn"))
        assertEquals(Source.PullRequest(api, 398), first("table remove"))
        assertEquals(Source.PullRequest(api, 401), first("#401"))
        assertEquals(Source.PullRequest(api, 401), first("audit-pages"))
        assertEquals(Source.PullRequest(api, 398), first("fischer"))
        assertTrue(SwitcherSearch.sections("qqqq", SwitcherScope.Sources, SwitcherInputs(followed = found)).isEmpty())
    }

    @Test
    fun commitsMatchByTheStartOfTheirShaOrTheirTitle() {
        val a = CommitInfo(ObjectId("1098b8f0b5bd33a26ce520279bbbccd37949b9c7"), "Drop the key hint", "jpark", 0)
        val b = CommitInfo(ObjectId("a5fdccfd40881d6b09a1a6574b7dd0d700bafb3f"), "Close the window even when a step fails", "jpark", 0)
        val commits = listOf(a, b).map { SwitcherItem.FollowedCommit(Source.Commit(api, CommitSha(it.id.value)), it) }
        val range = SwitcherItem.FollowedCommit(Source.CommitRange(api, CommitSha(a.id.value), CommitSha(b.id.value)), b)
        val found = FollowedResults.of(emptyList(), emptyMap(), origins, emptyList(), commits = commits + range)
        fun first(query: String) = sourceOf(SwitcherSearch.sections(query, SwitcherScope.Sources, SwitcherInputs(followed = found)).single().items.first())
        assertEquals(Source.Commit(api, CommitSha(a.id.value)), first("1098b8f0b5bd33a26ce520279bbbccd37949b9c7"))
        assertEquals(Source.Commit(api, CommitSha(a.id.value)), first("1098b8f"))
        assertEquals(Source.Commit(api, CommitSha(b.id.value)), first("windw step"))
        assertEquals(range.source, first("1098b8f..a5fdccf"))
        assertEquals(CommitQuery(null, "1098b8f"), CommitQuery.read(" 1098B8F "))
        assertNull(CommitQuery.read("abc12"))
        assertNull(CommitQuery.read("token"))
    }

    @Test
    fun branchesWithNothingAheadComeLast() {
        val branches = mapOf(api to listOf(LocalBranch("main", "x", 9, ahead = 0), LocalBranch("spike/token", "x", 1, ahead = 3)))
        val found = FollowedResults.of(emptyList(), branches, origins, emptyList())
        assertEquals(listOf("spike/token", "main"), found.map { (it as SwitcherItem.FollowedBranch).branch.name })
        val typed = SwitcherSearch.sections("main", SwitcherScope.Sources, SwitcherInputs(followed = found + SwitcherItem.FollowedBranch(Source.BranchAgainstBase(web, BranchName("maintenance")), LocalBranch("maintenance", "x", 0, ahead = 2))))
        assertEquals("maintenance", (typed.single().items.first() as SwitcherItem.FollowedBranch).branch.name)
    }

    @Test
    fun aPastedFolderIsReadAsARepoToFollow() {
        val folder = Path.of("").toAbsolutePath().toString()
        assertEquals(Path.of(folder), TypedPath.read("\"$folder\""))
        assertNull(TypedPath.read("api#398"))
        assertNull(TypedPath.read("token"))
    }

    private fun sourceOf(item: SwitcherItem): Source = when (item) {
        is SwitcherItem.FollowedPull -> item.source
        is SwitcherItem.FollowedBranch -> item.source
        is SwitcherItem.Recent -> item.source
        is SwitcherItem.FollowedCommit -> item.source
        else -> error("not followed")
    }

    @Test
    fun aPastedLinkLandsInTheCloneOfItsRepo() {
        val target = PastedTargets.read("https://github.com/lumen/api/pull/398/commits/b7e19d2", origins, origins.keys)!!
        assertTrue(target.link)
        assertEquals(Source.Commit(api, CommitSha("b7e19d2")), target.source)
        assertEquals(Source.PullRequest(api, 398), target.pullRequest)
        assertTrue(target.withinPull)
        assertEquals(2, OpenAs.of(target).size)
    }

    @Test
    fun aLinkToARepoWithNoCloneHereHasNowhereToOpen() {
        val target = PastedTargets.read("https://github.com/acme/other/pull/3", origins, origins.keys)!!
        assertNull(target.source)
        assertEquals(emptyList(), OpenAs.of(target))
    }

    @Test
    fun aRefOfAKnownRepoReadsAsATargetAndASearchDoesNot() {
        val target = PastedTargets.read("api#398", origins, origins.keys)!!
        assertEquals(Source.PullRequest(api, 398), target.source)
        assertEquals(1, OpenAs.of(target).size)
        assertNull(PastedTargets.read("tok", origins, origins.keys))
        assertNull(PastedTargets.read("nope#3", origins, origins.keys))
        assertNull(PastedTargets.read("remove token", origins, origins.keys))
    }

    @Test
    fun theLookupAsksGitHubOncePerQueryWhileItsAnswerIsFresh() = runTest {
        val data = FakeReviewData().apply { openPullList = listOf(FakePulls.pull(398, "Remove legacy token table")) }
        var now = 0L
        val lookup = FollowedLookup(data) { now }
        val repos = origins.values
        lookup.pulls(repos, "tok")
        lookup.pulls(repos, " TOK ")
        assertEquals(1, data.openPullQueries)
        now += SwitcherLimits.CACHE_MILLIS
        lookup.pulls(repos, "tok")
        assertEquals(2, data.openPullQueries)
    }
}

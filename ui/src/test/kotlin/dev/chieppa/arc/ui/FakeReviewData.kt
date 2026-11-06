package dev.chieppa.arc.ui

import dev.chieppa.arc.core.change.ChangeStatus
import dev.chieppa.arc.core.change.ChangedFile
import dev.chieppa.arc.core.change.CommitInfo
import dev.chieppa.arc.core.change.ObjectId
import dev.chieppa.arc.core.change.SourceRevisions
import dev.chieppa.arc.core.change.SourceSnapshot
import dev.chieppa.arc.core.mark.PatchFingerprint
import dev.chieppa.arc.core.mark.PatchHash
import dev.chieppa.arc.core.mark.ReplayOutcome
import dev.chieppa.arc.core.mark.ReviewedRevisions
import dev.chieppa.arc.core.pane.Revision
import dev.chieppa.arc.core.source.RepoName
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.core.source.SourceRefs
import dev.chieppa.arc.ui.services.ReviewData
import java.nio.file.Path

/**
 * A repo held in memory: one range source, `demo@aaaa..bbbb`, changing `app/Main.kt` and `app/util/Text.kt` and
 * adding `docs/Guide.md`, with every file's text at both revisions.
 */
class FakeReviewData : ReviewData {
    val repo = RepoName("demo")
    val base = ObjectId("a".repeat(40))
    val head = ObjectId("b".repeat(40))
    val source: Source = SourceRefs.parse("demo@${"a".repeat(40)}..${"b".repeat(40)}")!!

    private val oldTexts = mapOf(
        "app/Main.kt" to (1..40).joinToString("\n") { "line $it" } + "\n",
        "app/util/Text.kt" to "fun text() = \"old\"\n",
    )
    private val newTexts = mapOf(
        "app/Main.kt" to (1..40).joinToString("\n") { if (it == 20) "line twenty" else "line $it" } + "\nfind me\nfind me too\n",
        "app/util/Text.kt" to "fun text() = \"new\"\n",
        "docs/Guide.md" to "# Guide\n\nRead **this** first.\n",
        "README.md" to "Hello\n",
    )

    val files = listOf(
        ChangedFile("app/Main.kt", ChangeStatus.Modified, added = 3, removed = 1),
        ChangedFile("app/util/Text.kt", ChangeStatus.Modified, added = 1, removed = 1),
        ChangedFile("docs/Guide.md", ChangeStatus.Added, added = 3, removed = 0),
    )

    override suspend fun read(source: Source): SourceSnapshot = SourceSnapshot(
        source = source,
        revisions = SourceRevisions(base, head),
        title = "Rotate tokens",
        commits = listOf(CommitInfo(head, "Rotate tokens", "Test", 0)),
        files = files,
        fingerprints = files.associate { it.path to PatchFingerprint(PatchHash("meta"), PatchHash("exact-${it.path}")) },
    )

    override suspend fun readCommit(source: Source, commit: ObjectId): SourceSnapshot = read(source)

    override suspend fun replay(repo: RepoName, reviewed: ReviewedRevisions, now: SourceRevisions): ReplayOutcome? = null

    override suspend fun readFile(repo: RepoName, revision: Revision, path: String): ByteArray? {
        val texts = if (revision.value == base.value) oldTexts else newTexts
        return texts[path]?.toByteArray()
    }

    override suspend fun listFiles(repo: RepoName, revision: Revision): List<String> = newTexts.keys.sorted()

    override suspend fun isRepo(dir: Path): Boolean = true
}

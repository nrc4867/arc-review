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
import dev.chieppa.arc.core.revision.RecordedHead
import dev.chieppa.arc.core.revision.RevisionHistory
import dev.chieppa.arc.core.revision.SourceRevision
import dev.chieppa.arc.core.source.RepoName
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.core.source.SourceRefs
import dev.chieppa.arc.ui.services.ReviewData
import org.jetbrains.skia.Color
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Surface
import java.nio.file.Path

/**
 * A repo held in memory: one range source, `demo@aaaa..bbbb`, changing `app/Main.kt` and `app/util/Text.kt` and
 * adding `docs/Guide.md`, with every file's text at both revisions. With [more], it also deletes `old/Gone.kt`, changes
 * `scripts/run.sh` without changing a line, and adds the binary `img/logo.png`. With [pictures], it also changes the
 * PNG `img/photo.png` from 30 by 20 pixels to 40 by 20 and adds the SVG `img/icon.svg`, 16 pixels square.
 */
class FakeReviewData(more: Boolean = false, pictures: Boolean = false) : ReviewData {
    val repo = RepoName("demo")
    val base = ObjectId("a".repeat(40))
    val head = ObjectId("b".repeat(40))
    val source: Source = SourceRefs.parse("demo@${"a".repeat(40)}..${"b".repeat(40)}")!!

    private val oldTexts = mapOf(
        "app/Main.kt" to (1..40).joinToString("\n") { "line $it" } + "\n",
        "app/util/Text.kt" to "fun text() = \"old\"\n",
        "old/Gone.kt" to "fun gone() = 1\nfun goneToo() = 2\n",
        "scripts/run.sh" to "echo run\n",
    )
    private val newTexts = mapOf(
        "app/Main.kt" to (1..40).joinToString("\n") { if (it == 20) "line twenty" else "line $it" } + "\nfind me\nfind me too\n",
        "app/util/Text.kt" to "fun text() = \"new\"\n",
        "docs/Guide.md" to "# Guide\n\nRead **this** first.\n",
        "README.md" to "Hello\n",
        "scripts/run.sh" to "echo run\n",
    )

    private val binary = mapOf("img/logo.png" to byteArrayOf(0x89.toByte(), 0x50, 0, 0, 0x1a), "img/photo.png" to png(40, 20))
    private val oldBinary = mapOf("img/photo.png" to png(30, 20))
    private val svg = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"16\" height=\"16\"><circle cx=\"8\" cy=\"8\" r=\"6\"/></svg>"

    val files = listOf(
        ChangedFile("app/Main.kt", ChangeStatus.Modified, added = 3, removed = 1),
        ChangedFile("app/util/Text.kt", ChangeStatus.Modified, added = 1, removed = 1),
        ChangedFile("docs/Guide.md", ChangeStatus.Added, added = 3, removed = 0),
    ) + if (!more) emptyList() else listOf(
        ChangedFile("old/Gone.kt", ChangeStatus.Deleted, added = 0, removed = 2),
        ChangedFile("scripts/run.sh", ChangeStatus.TypeChanged, added = 0, removed = 0),
        ChangedFile("img/logo.png", ChangeStatus.Added, added = null, removed = null),
    ) + if (!pictures) emptyList() else listOf(
        ChangedFile("img/photo.png", ChangeStatus.Modified, added = null, removed = null),
        ChangedFile("img/icon.svg", ChangeStatus.Added, added = 1, removed = 0),
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
        if (revision.value == head.value) binary[path]?.let { return it }
        if (revision.value == base.value) oldBinary[path]?.let { return it }
        if (revision.value == head.value && path == "img/icon.svg") return svg.toByteArray()
        val texts = if (revision.value == base.value) oldTexts else newTexts
        return texts[path]?.toByteArray()
    }

    override suspend fun listFiles(repo: RepoName, revision: Revision): List<String> = newTexts.keys.sorted()

    override suspend fun isRepo(dir: Path): Boolean = true

    /** The first head the source had, before [head]: it changed only `app/Main.kt`, as it does now. */
    val firstHead = ObjectId("c".repeat(40))

    /** The commits picked and compared, older first, by the last call to [differingFiles]. */
    var compared: Pair<SourceRevisions, SourceRevisions>? = null

    /** Two revisions: the first changes `app/Main.kt`; the second, the head, changes every file. */
    override suspend fun revisions(source: Source, recorded: List<RecordedHead>): RevisionHistory {
        val now = read(source)
        val first = SourceRevision(
            1, firstHead, base, 100,
            commits = listOf(CommitInfo(firstHead, "Start rotating", "Test", 100)),
            fingerprints = mapOf("app/Main.kt" to now.fingerprints.getValue("app/Main.kt")),
            pinned = true,
        )
        val second = SourceRevision(2, head, base, 200, commits = now.commits, fingerprints = now.fingerprints, pinned = true)
        return RevisionHistory(source, listOf(first, second))
    }

    override suspend fun differingFiles(repo: RepoName, a: SourceRevisions, b: SourceRevisions): Set<String> {
        compared = a to b
        return files.map { it.path }.filter { it != "app/Main.kt" }.toSet()
    }

    private companion object {
        /** A PNG of one color, [width] by [height] pixels. */
        fun png(width: Int, height: Int): ByteArray {
            val surface = Surface.makeRasterN32Premul(width, height)
            surface.canvas.clear(Color.BLUE)
            return surface.makeImageSnapshot().encodeToData(EncodedImageFormat.PNG)!!.bytes
        }
    }
}

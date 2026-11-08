package dev.chieppa.arc.ui.conversation

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.chieppa.arc.core.change.ObjectId
import dev.chieppa.arc.core.change.SourceSnapshot
import dev.chieppa.arc.core.conversation.LineMap
import dev.chieppa.arc.core.conversation.LineSide
import dev.chieppa.arc.core.conversation.LocalComment
import dev.chieppa.arc.core.pane.Revision
import dev.chieppa.arc.core.source.RepoName
import dev.chieppa.arc.ui.services.ReviewData
import dev.chieppa.arc.ui.util.TextDecoding
import dev.chieppa.stretch.diff.LineKind
import dev.chieppa.stretch.diff.diffTexts
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

/**
 * Where the person's comments sit as their sources move, and which lines of each changed file the host takes comments
 * on. A comment written at an older head is moved along with its lines to the head now; one whose line is gone is
 * marked with the head that removed it. Files are read through [data] and compared on [work], off the UI thread.
 */
@Stable
class StagedPlacer(private val data: ReviewData, private val work: CoroutineDispatcher) {
    /** Where a comment's line is at [head]: [line], or null when the line is gone. */
    data class Placed(val head: ObjectId, val line: Int?)

    private data class FileKey(val head: ObjectId, val path: String, val side: LineSide)

    var placed by mutableStateOf<Map<String, Placed>>(emptyMap())
        private set

    private var commentable by mutableStateOf<Map<FileKey, Set<Int>>>(emptyMap())

    /** The line [comment] sits on in [snapshot] now, or null when its line is gone; its own line until worked out. */
    fun lineOf(comment: LocalComment, snapshot: SourceSnapshot?): Int? {
        val anchor = comment.anchor ?: return null
        val head = snapshot?.revisions?.head ?: return anchor.line
        if (anchor.madeOn == null || anchor.madeOn == head || anchor.side == LineSide.Old) return anchor.line
        val known = placed[comment.id]?.takeIf { it.head == head } ?: return anchor.line
        return known.line
    }

    /** The lines on [side] of [path] in [snapshot]'s change that the host takes comments on, once worked out. */
    fun commentable(snapshot: SourceSnapshot, path: String, side: LineSide): Set<Int>? =
        commentable[FileKey(snapshot.revisions.head, path, side)]

    /** Works out where each of [comments] sits in [snapshot], and the commentable lines of the files they are on. */
    suspend fun update(snapshot: SourceSnapshot, comments: List<LocalComment>) {
        val head = snapshot.revisions.head
        val repo = snapshot.source.repo
        for (comment in comments) {
            val anchor = comment.anchor ?: continue
            val madeOn = anchor.madeOn
            if (madeOn != null && madeOn != head && anchor.side == LineSide.New && placed[comment.id]?.head != head) {
                val line = withContext(work) {
                    val old = text(repo, madeOn, anchor.path)
                    val new = text(repo, head, anchor.path)
                    if (old == null || new == null) null else LineMap.of(old.lines(), new.lines()).map(anchor.line)
                }
                placed = placed + (comment.id to Placed(head, line))
            }
            val file = snapshot.file(anchor.path) ?: continue
            if (FileKey(head, file.path, anchor.side) in commentable) continue
            val lines = withContext(work) { hunkLines(repo, snapshot, file.path, file.oldPath ?: file.path) }
            commentable = commentable + (FileKey(head, file.path, LineSide.New) to lines.first) + (FileKey(head, file.path, LineSide.Old) to lines.second)
        }
    }

    private suspend fun hunkLines(repo: RepoName, snapshot: SourceSnapshot, path: String, oldPath: String): Pair<Set<Int>, Set<Int>> {
        val old = text(repo, snapshot.revisions.base, oldPath).orEmpty()
        val new = text(repo, snapshot.revisions.head, path).orEmpty()
        val diff = diffTexts(old, new, oldPath, path)
        val lines = diff.hunks.flatMap { it.lines }
        val newSide = lines.filter { it.kind != LineKind.Removed }.mapNotNull { it.newNumber }.toSet()
        val oldSide = lines.filter { it.kind != LineKind.Added }.mapNotNull { it.oldNumber }.toSet()
        return newSide to oldSide
    }

    private suspend fun text(repo: RepoName, at: ObjectId, path: String): String? =
        data.readFile(repo, Revision(at.value), path)?.let(TextDecoding::decode)
}

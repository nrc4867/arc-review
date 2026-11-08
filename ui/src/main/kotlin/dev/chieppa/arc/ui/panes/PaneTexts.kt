package dev.chieppa.arc.ui.panes

import dev.chieppa.arc.core.pane.Pane
import dev.chieppa.arc.core.pane.Revision
import dev.chieppa.arc.core.source.RepoName
import dev.chieppa.arc.ui.review.PaneFactory
import dev.chieppa.arc.ui.services.ReviewData
import dev.chieppa.arc.ui.util.TextDecoding
import dev.chieppa.stretch.diff.FileDiff
import dev.chieppa.stretch.diff.diffTexts
import dev.chieppa.stretch.ui.decodeImage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

/**
 * Reads the text a pane shows through [data], and its diff when it has a base, worked out on [work], off the UI thread, and draws
 * a binary file or an SVG as a picture where it is one. The most recent files are kept, so switching back to a pane is
 * instant.
 */
class PaneTexts(private val data: ReviewData, private val work: CoroutineDispatcher) {
    private val recent = object : LinkedHashMap<Triple<RepoName, Revision, String>, ByteArray?>(CAPACITY, LOAD_FACTOR, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Triple<RepoName, Revision, String>, ByteArray?>?) = size > CAPACITY
    }

    suspend fun load(pane: Pane): PaneText {
        val new = read(pane.repo, pane.revision, pane.path)
        val old = pane.base?.let { read(pane.repo, it, pane.oldPath ?: pane.path) }
        val oldPath = pane.oldPath ?: pane.path
        if ((new != null && TextDecoding.isBinary(new)) || (old != null && TextDecoding.isBinary(old))) {
            val (oldImage, newImage) = pictures(old, new)
            val diff = FileDiff(oldPath.takeIf { old != null }, pane.path.takeIf { new != null }, binary = true)
            val changed = old != null && new != null && !old.contentEquals(new)
            return PaneText(null, null, diff, binary = true, binaryChanged = changed, oldImage = oldImage, newImage = newImage)
        }
        val oldText = old?.let(TextDecoding::decode)
        val newText = new?.let(TextDecoding::decode)
        val diff = if (pane.base == null) {
            null
        } else {
            withContext(work) {
                diffTexts(oldText.orEmpty(), newText.orEmpty(), oldPath.takeIf { oldText != null }, pane.path.takeIf { newText != null })
            }
        }
        val (oldImage, newImage) = if (PaneFactory.isSvg(pane.path)) pictures(old, new) else null to null
        return PaneText(oldText, newText, diff, oldImage = oldImage, newImage = newImage)
    }

    /** [old] and [new] drawn, off the UI thread; [old] alone is left out when it is the same as [new]. */
    private suspend fun pictures(old: ByteArray?, new: ByteArray?) = withContext(work) {
        val same = old != null && new != null && old.contentEquals(new)
        (if (same) null else old?.let(::decodeImage)) to new?.let(::decodeImage)
    }

    private suspend fun read(repo: RepoName, revision: Revision, path: String): ByteArray? {
        val key = Triple(repo, revision, path)
        synchronized(recent) { if (recent.containsKey(key)) return recent[key] }
        val bytes = data.readFile(repo, revision, path)
        synchronized(recent) { recent[key] = bytes }
        return bytes
    }

    private companion object {
        const val CAPACITY = 64
        const val LOAD_FACTOR = 0.75f
    }
}

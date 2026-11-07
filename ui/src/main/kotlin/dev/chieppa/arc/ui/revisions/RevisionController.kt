package dev.chieppa.arc.ui.revisions

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.chieppa.arc.core.change.ObjectId
import dev.chieppa.arc.core.change.SourceRevisions
import dev.chieppa.arc.core.pane.Revision
import dev.chieppa.arc.core.revision.SourceRevision
import dev.chieppa.arc.core.revision.TreePathOrder
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.ui.review.PaneController
import dev.chieppa.arc.ui.review.ReviewState
import dev.chieppa.arc.ui.services.ReviewData
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Each source's revisions: reading them in the background from the heads the review recorded, the two commits picked
 * in the Commits drawer and the files whose change differs between them, and opening a file at a revision or its
 * change between two. A new read of a source cancels the one before it; the old revisions stay until it is done.
 */
class RevisionController(
    private val state: ReviewState,
    private val data: ReviewData,
    private val scope: CoroutineScope,
    private val panes: PaneController,
) {
    private val jobs = mutableMapOf<Source, Job>()

    /** The commits picked for comparing in each source, in the order they were picked; at most two. */
    var picks by mutableStateOf<Map<Source, List<ObjectId>>>(emptyMap())
        private set

    /** The paths whose change differs between two picked commits, by the pair, older first, once worked out. */
    var differing by mutableStateOf<Map<Pair<ObjectId, ObjectId>, Set<String>>>(emptyMap())
        private set

    /** Reads [source]'s revisions again, with the checks on their heads. */
    fun load(source: Source) {
        jobs[source]?.cancel()
        jobs[source] = scope.launch {
            val read = try {
                val history = data.revisions(source, state.review.headsOf(source))
                RevisionsRead(history, data.checks(source.repo, history.revisions.map { it.head }))
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                return@launch
            }
            if (source in state.review.sources) state.revisions = state.revisions + (source to read)
        }
    }

    /** Picks [commit] of [source] for comparing, or unpicks it; a third pick lets the first go. */
    fun togglePick(source: Source, commit: ObjectId) {
        val now = picks[source].orEmpty()
        val next = if (commit in now) now - commit else (now + commit).takeLast(PICKS)
        picks = picks + (source to next)
        comparison(source)?.let { workOutDiffering(source, it) }
    }

    /** The two picked commits of [source] with the revisions that brought them, older first; null unless two are picked and read. */
    fun comparison(source: Source): Comparison? {
        val history = state.revisions[source]?.history ?: return null
        val picked = picks[source].orEmpty().takeIf { it.size == PICKS } ?: return null
        val order = history.commits.map { it.first.id } + history.revisions.map { it.head }
        val (older, newer) = picked.sortedBy { order.indexOf(it) }
        val from = history.revisionOf(older) ?: return null
        val to = history.revisionOf(newer) ?: return null
        return Comparison(SourceRevisions(from.base, older), SourceRevisions(to.base, newer), from, to)
    }

    /** Opens every file whose change differs between [source]'s two picks as a diff from the older to the newer. */
    fun compare(source: Source) {
        val comparison = comparison(source) ?: return
        val paths = differing[comparison.key] ?: return
        val ids = paths.sortedWith(TreePathOrder).map { path ->
            panes.openDiff(source.repo, path, Revision(comparison.from.head.value), Revision(comparison.to.head.value), source)
        }
        ids.firstOrNull()?.let { panes.show(it.value) }
    }

    /** Opens [path] of [source] whole at [revision]'s head. */
    fun openAt(source: Source, revision: SourceRevision, path: String) {
        panes.openFile(source.repo, Revision(revision.head.value), path, source)
    }

    /** Opens [path]'s change from [from]'s head to [to]'s, or from [to]'s base when [from] is null. */
    fun openDiff(source: Source, from: SourceRevision?, to: SourceRevision, path: String) {
        val base = from?.head ?: to.base
        val id = panes.openDiff(source.repo, path, Revision(base.value), Revision(to.head.value), source)
        panes.show(id.value)
    }

    private fun workOutDiffering(source: Source, comparison: Comparison) {
        if (comparison.key in differing) return
        scope.launch {
            val paths = try {
                data.differingFiles(source.repo, comparison.from, comparison.to)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                return@launch
            }
            differing = differing + (comparison.key to paths)
        }
    }

    private companion object {
        const val PICKS = 2
    }
}

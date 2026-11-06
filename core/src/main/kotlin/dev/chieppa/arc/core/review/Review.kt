package dev.chieppa.arc.core.review

import dev.chieppa.arc.core.mark.FileMark
import dev.chieppa.arc.core.pane.Pane
import dev.chieppa.arc.core.source.RepoName
import dev.chieppa.arc.core.source.Source

/**
 * A saved, named workspace: the [sources] under review, the [panes] open on them and the [marks] made so far. While it
 * has sources, one of them is [active] and drives the file tree. Every change returns a new review.
 */
data class Review(
    val id: ReviewId,
    val name: String,
    val sources: List<Source> = emptyList(),
    val active: Source? = sources.firstOrNull(),
    val panes: List<Pane> = emptyList(),
    val marks: List<FileMark> = emptyList(),
) {
    init {
        require(sources.toSet().size == sources.size) { "A source is in a review once" }
        require(if (sources.isEmpty()) active == null else active in sources) { "The active source is one of the review's" }
        require(panes.map { it.id }.toSet().size == panes.size) { "Pane ids are unique in a review" }
        require(marks.map { it.repo to it.path }.toSet().size == marks.size) { "A file has one mark" }
    }

    /** The review with [source] added at the end, active if nothing was. A source already here stays where it is. */
    fun add(source: Source): Review =
        if (source in sources) this else copy(sources = sources + source, active = active ?: source)

    /** The review with [source] driving the tree. A source not in the review changes nothing. */
    fun activate(source: Source): Review = if (source in sources) copy(active = source) else this

    /**
     * The review without [source]. When it was active, the source after it becomes active, or the one before it when
     * it was last.
     */
    fun remove(source: Source): Review {
        val index = sources.indexOf(source)
        if (index < 0) return this
        val left = sources - source
        val next = if (source == active) left.getOrNull(index) ?: left.lastOrNull() else active
        return copy(sources = left, active = next)
    }

    /** The mark on [path] in [repo], if it has one. */
    fun markOf(repo: RepoName, path: String): FileMark? = marks.firstOrNull { it.repo == repo && it.path == path }

    /** The review with [mark] in place of any mark on the same file. */
    fun mark(mark: FileMark): Review =
        copy(marks = marks.filterNot { it.repo == mark.repo && it.path == mark.path } + mark)

    /** The review with no mark on [path] in [repo]. */
    fun unmark(repo: RepoName, path: String): Review =
        copy(marks = marks.filterNot { it.repo == repo && it.path == path })
}

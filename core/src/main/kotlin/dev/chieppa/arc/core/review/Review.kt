package dev.chieppa.arc.core.review

import dev.chieppa.arc.core.mark.FileMark
import dev.chieppa.arc.core.pane.Pane
import dev.chieppa.arc.core.pane.PaneId
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
        require(marks.map { it.source to it.path }.toSet().size == marks.size) { "A file has one mark in a source" }
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

    /** The mark on [path] in [source], if it has one. */
    fun markOf(source: Source, path: String): FileMark? = marks.firstOrNull { it.source == source && it.path == path }

    /** The marks on files of [source], by path. */
    fun marksIn(source: Source): Map<String, FileMark> = marks.filter { it.source == source }.associateBy { it.path }

    /** The review with [mark] in place of any mark on the same file in the same source. */
    fun mark(mark: FileMark): Review = mark(listOf(mark))

    /** The review with each of [added] in place of any mark on the same file in the same source. */
    fun mark(added: List<FileMark>): Review {
        val keys = added.map { it.source to it.path }.toSet()
        return copy(marks = marks.filterNot { (it.source to it.path) in keys } + added.distinctBy { it.source to it.path })
    }

    /** The review with no mark on [paths] in [source]. */
    fun unmark(source: Source, paths: Collection<String>): Review =
        copy(marks = marks.filterNot { it.source == source && it.path in paths })

    /** The review with no mark on [path] in [source]. */
    fun unmark(source: Source, path: String): Review = unmark(source, listOf(path))

    /** The review with [pane] added, or in place of the pane with its id. */
    fun withPane(pane: Pane): Review = copy(panes = panes.filterNot { it.id == pane.id } + pane)

    /** The review without the pane [id]. */
    fun withoutPane(id: PaneId): Review = copy(panes = panes.filterNot { it.id == id })
}

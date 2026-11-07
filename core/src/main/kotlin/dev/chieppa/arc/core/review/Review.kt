package dev.chieppa.arc.core.review

import dev.chieppa.arc.core.mark.FileMark
import dev.chieppa.arc.core.pane.Page
import dev.chieppa.arc.core.pane.Pane
import dev.chieppa.arc.core.pane.PaneId
import dev.chieppa.arc.core.revision.RecordedHead
import dev.chieppa.arc.core.source.Source

/**
 * A saved, named workspace: the [sources] under review, the [panes] open on them, the [pages] open beside them, and
 * the [marks] made so far. While it has sources, one of them is [active] and drives the file tree. [heads] are the
 * heads Arc has read each source at, oldest first, which its revisions are made from. Every change returns a new review.
 */
data class Review(
    val id: ReviewId,
    val name: String,
    val sources: List<Source> = emptyList(),
    val active: Source? = sources.firstOrNull(),
    val panes: List<Pane> = emptyList(),
    val marks: List<FileMark> = emptyList(),
    val pages: List<Page> = emptyList(),
    val heads: Map<Source, List<RecordedHead>> = emptyMap(),
) {
    init {
        require(sources.toSet().size == sources.size) { "A source is in a review once" }
        require(if (sources.isEmpty()) active == null else active in sources) { "The active source is one of the review's" }
        require((panes.map { it.id } + pages.map { it.id }).toSet().size == panes.size + pages.size) { "Pane and page ids are unique in a review" }
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

    /** The review with [page] added, or in place of the page with its id. */
    fun withPage(page: Page): Review = copy(pages = pages.filterNot { it.id == page.id } + page)

    /** The review without the page [id]. */
    fun withoutPage(id: PaneId): Review = copy(pages = pages.filterNot { it.id == id })

    /** The heads Arc has read [source] at, oldest first. */
    fun headsOf(source: Source): List<RecordedHead> = heads[source].orEmpty()

    /** The review with [head] recorded for [source], unless a head with its commit is recorded already. */
    fun record(source: Source, head: RecordedHead): Review {
        val known = headsOf(source)
        if (known.any { it.head == head.head }) return this
        return copy(heads = heads + (source to known + head))
    }

    /** The ids of every pinned pane and page, which follow into each source's layout. */
    val pinnedIds: List<String> get() = panes.filter { it.pinned }.map { it.id.value } + pages.filter { it.pinned }.map { it.id.value }

    /** The ids of every pane and page. */
    val tabIds: Set<String> get() = (panes.map { it.id.value } + pages.map { it.id.value }).toSet()
}

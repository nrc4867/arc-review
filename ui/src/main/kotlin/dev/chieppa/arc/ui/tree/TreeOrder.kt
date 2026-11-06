package dev.chieppa.arc.ui.tree

import dev.chieppa.arc.core.mark.FileReviewState
import dev.chieppa.arc.core.source.Source
import dev.chieppa.stretch.ui.TreeNode

/** The order files are read in: as the tree lists them, the set-aside fold left out. */
object TreeOrder {
    /** The file rows of [source] in [roots], top to bottom. */
    fun files(roots: List<TreeNode<FileTreeItem>>, source: Source): List<FileTreeItem.FileRow> {
        val sourceNode = roots.firstOrNull { it.item.source == source } ?: return emptyList()
        return buildList { collect(sourceNode, this) }
    }

    /** The file after (or before, with [step] −1) [current] in [files], or the first (or last) one when there is none. */
    fun step(files: List<FileTreeItem.FileRow>, current: String?, step: Int): FileTreeItem.FileRow? {
        if (files.isEmpty()) return null
        val at = files.indexOfFirst { it.file.path == current }
        if (at < 0) return if (step > 0) files.first() else files.last()
        return files.getOrNull(at + step)
    }

    /** The first file after [current] not yet reviewed, wrapping round to the top, or null when all are. */
    fun nextToReview(files: List<FileTreeItem.FileRow>, current: String?): FileTreeItem.FileRow? {
        val at = files.indexOfFirst { it.file.path == current }
        val after = files.drop(at + 1) + files.take(at + 1)
        return after.firstOrNull { it.file.path != current && it.state != FileReviewState.Reviewed }
    }

    private fun collect(node: TreeNode<FileTreeItem>, into: MutableList<FileTreeItem.FileRow>) {
        when (val item = node.item) {
            is FileTreeItem.FileRow -> into += item
            is FileTreeItem.AsideRow -> Unit
            else -> node.children.forEach { collect(it, into) }
        }
    }
}

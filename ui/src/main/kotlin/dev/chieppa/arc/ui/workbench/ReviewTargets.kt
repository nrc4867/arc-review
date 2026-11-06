package dev.chieppa.arc.ui.workbench

import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.ui.tree.FileTreeItem
import dev.chieppa.arc.ui.tree.ReviewTree
import dev.chieppa.stretch.ui.TreeNode

/**
 * The files a review key acts on: the tree's selection while the tree has the keyboard, a selected folder standing for
 * every file under it, and otherwise the file being read. Grouped by source.
 */
object ReviewTargets {
    fun of(workbench: Workbench): Map<Source, List<String>> {
        if (workbench.treeFocused && workbench.treeState.selection.isNotEmpty()) {
            return selected(ReviewTree.roots(workbench.state), workbench.treeState.selection)
        }
        val (source, path) = workbench.marks.current ?: return emptyMap()
        return mapOf(source to listOf(path))
    }

    /** The files under the rows with [keys] in [roots], by source. */
    fun selected(roots: List<TreeNode<FileTreeItem>>, keys: Set<Any>): Map<Source, List<String>> {
        val found = linkedMapOf<Source, MutableSet<String>>()
        fun files(node: TreeNode<FileTreeItem>, into: MutableSet<String>) {
            val item = node.item
            if (item is FileTreeItem.FileRow) into += item.file.path
            node.children.forEach { files(it, into) }
        }
        fun walk(node: TreeNode<FileTreeItem>) {
            if (node.item.key in keys) files(node, found.getOrPut(node.item.source) { linkedSetOf() }) else node.children.forEach(::walk)
        }
        roots.forEach(::walk)
        return found.mapValues { it.value.toList() }
    }
}

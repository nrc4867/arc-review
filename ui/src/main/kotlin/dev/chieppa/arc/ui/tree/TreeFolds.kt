package dev.chieppa.arc.ui.tree

import dev.chieppa.stretch.ui.TreeNode
import dev.chieppa.stretch.ui.treeParents

/** Opening and closing parts of the file tree at once. */
object TreeFolds {
    /** The keys of every row that has rows under it, in the row [key] and below, or in the whole tree for null. */
    fun parents(roots: List<TreeNode<FileTreeItem>>, key: String?): Set<Any> {
        val nodes = if (key == null) roots else listOfNotNull(find(roots, key))
        return treeParents(nodes) { it.key }
    }

    private fun find(nodes: List<TreeNode<FileTreeItem>>, key: String): TreeNode<FileTreeItem>? {
        for (node in nodes) {
            if (node.item.key == key) return node
            find(node.children, key)?.let { return it }
        }
        return null
    }
}

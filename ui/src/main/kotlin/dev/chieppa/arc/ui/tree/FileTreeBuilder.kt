package dev.chieppa.arc.ui.tree

import dev.chieppa.arc.core.change.ChangedFile
import dev.chieppa.arc.core.mark.FileReviewState
import dev.chieppa.arc.core.source.Source
import dev.chieppa.stretch.ui.TreeNode

/**
 * Builds the file tree: one node per source, its changed files in folders under it, folders first and each level in
 * name order, a folder that holds only one folder joined with it, and the files set aside in a fold at the end.
 */
object FileTreeBuilder {
    private const val SLASH = '/'

    fun build(sources: List<SourceRows>): List<TreeNode<FileTreeItem>> = sources.map { rows ->
        val files = rows.files.orEmpty()
        val (aside, shown) = files.partition { rows.states[it.path] == FileReviewState.SetAside }
        val children = folder(rows, "", shown).toMutableList()
        if (aside.isNotEmpty()) {
            val fold = FileTreeItem.AsideRow(rows.source, aside.size, aside.map { it.name })
            children += TreeNode(fold, aside.map { TreeNode(fileRow(rows, it)) })
        }
        TreeNode(FileTreeItem.SourceRow(rows.source, rows.title, rows.progress, rows.failed, rows.ended), children)
    }

    private fun folder(rows: SourceRows, prefix: String, files: List<ChangedFile>): List<TreeNode<FileTreeItem>> {
        val (here, below) = files.partition { relative(it, prefix).indexOf(SLASH) < 0 }
        val folders = below.groupBy { relative(it, prefix).substringBefore(SLASH) }.toSortedMap()
        val folderNodes = folders.map { (name, inside) -> folderNode(rows, prefix + name, name, inside) }
        return folderNodes + here.sortedBy { it.name.lowercase() }.map { TreeNode(fileRow(rows, it)) }
    }

    private fun folderNode(rows: SourceRows, path: String, label: String, files: List<ChangedFile>): TreeNode<FileTreeItem> {
        val prefix = path + SLASH
        val children = folder(rows, prefix, files)
        val only = children.singleOrNull()?.item as? FileTreeItem.FolderRow
        if (only != null) {
            val joined = only.copy(label = label + SLASH + only.label)
            return TreeNode(joined, children.single().children)
        }
        val reviewed = files.count { rows.states[it.path] == FileReviewState.Reviewed }
        val marked = when (reviewed) {
            files.size -> true
            0 -> false
            else -> null
        }
        return TreeNode(FileTreeItem.FolderRow(rows.source, path, label, files.size, marked), children)
    }

    private fun fileRow(rows: SourceRows, file: ChangedFile) =
        FileTreeItem.FileRow(rows.source, file, rows.states[file.path] ?: FileReviewState.NotReviewed)

    private fun relative(file: ChangedFile, prefix: String) = file.path.removePrefix(prefix)
}

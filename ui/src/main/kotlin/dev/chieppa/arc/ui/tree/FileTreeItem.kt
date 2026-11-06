package dev.chieppa.arc.ui.tree

import dev.chieppa.arc.core.change.ChangedFile
import dev.chieppa.arc.core.mark.FileReviewState
import dev.chieppa.arc.core.mark.ReviewProgress
import dev.chieppa.arc.core.source.Source

/** One row of the file tree, under the source it belongs to. */
sealed interface FileTreeItem {
    val key: String
    val source: Source

    /** A source, heading its files: its title and progress once read, and whether reading it failed. */
    data class SourceRow(override val source: Source, val title: String, val progress: ReviewProgress?, val failed: Boolean) : FileTreeItem {
        override val key: String get() = TreeKeys.source(source)
    }

    /**
     * A folder, shown as [label], which joins folders that hold only one folder. [marked] is true when every file under
     * it is reviewed, false when none is, and null when some are.
     */
    data class FolderRow(override val source: Source, val path: String, val label: String, val files: Int, val marked: Boolean?) : FileTreeItem {
        override val key: String get() = TreeKeys.folder(source, path)
    }

    data class FileRow(override val source: Source, val file: ChangedFile, val state: FileReviewState) : FileTreeItem {
        override val key: String get() = TreeKeys.file(source, file.path)
    }

    /** The fold holding the files set aside. */
    data class AsideRow(override val source: Source, val files: Int, val names: List<String>) : FileTreeItem {
        override val key: String get() = TreeKeys.aside(source)
    }
}

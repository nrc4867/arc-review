package dev.chieppa.arc.ui.tree

import dev.chieppa.arc.core.change.ChangedFile

/** A file's added and removed line counts as a tree row shows them, `+48` and `−19`, each left out when zero. */
data class LineCounts(val added: String?, val removed: String?) {
    companion object {
        private const val PLUS = "+"
        private const val MINUS = "−"

        fun of(file: ChangedFile): LineCounts = LineCounts(
            file.added?.takeIf { it > 0 }?.let { PLUS + it },
            file.removed?.takeIf { it > 0 }?.let { MINUS + it },
        )
    }
}

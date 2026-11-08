package dev.chieppa.arc.index

/** Every parsed file of one [target], by path. */
class IndexedSnapshot(val target: IndexTarget, val files: List<IndexedFile>) {
    private val byPath = files.associateBy { it.path }

    fun file(path: String): IndexedFile? = byPath[path]
}

package dev.chieppa.arc.core.revision

/** Orders file paths as a tree lists them: folder by folder, a folder's subfolders before its files, each by name. */
object TreePathOrder : Comparator<String> {
    override fun compare(a: String, b: String): Int {
        val left = a.split('/')
        val right = b.split('/')
        for (i in 0 until minOf(left.size, right.size)) {
            val leftFolder = i < left.lastIndex
            val rightFolder = i < right.lastIndex
            if (leftFolder != rightFolder) return if (leftFolder) -1 else 1
            val byName = left[i].compareTo(right[i], ignoreCase = true)
            if (byName != 0) return byName
        }
        return left.size - right.size
    }
}

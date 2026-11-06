package dev.chieppa.arc.git.parse

import dev.chieppa.arc.core.change.ObjectId

/** What `git merge-tree --write-tree --name-only -z` wrote: the merged tree and the paths that conflicted. */
class MergeTreeResult(val tree: ObjectId, val conflicted: Set<String>)

/** Reads `git merge-tree --write-tree --name-only -z` output: the tree, then conflicted paths up to an empty field. */
object MergeTreeParser {
    fun parse(output: ByteArray): MergeTreeResult {
        val reader = NulReader(output)
        val tree = ObjectId(reader.field().trim())
        val conflicted = buildSet {
            while (!reader.atEnd) {
                val path = reader.field()
                if (path.isEmpty()) break
                add(path)
            }
        }
        return MergeTreeResult(tree, conflicted)
    }
}

package dev.chieppa.arc.git.parse

import dev.chieppa.arc.core.change.ObjectId
import dev.chieppa.arc.git.repo.TreeBlob

/** Reads `git ls-tree -r -l -z` output: `<mode> <type> <object> <size>\t<path>` records, keeping the blobs. */
internal object TreeBlobParser {
    private const val META_PARTS = 4
    private const val BLOB = "blob"

    fun parse(output: ByteArray): List<TreeBlob> {
        val reader = NulReader(output)
        return buildList {
            while (!reader.atEnd) {
                val record = reader.field()
                val tab = record.indexOf('\t')
                if (tab < 0) continue
                val meta = record.substring(0, tab).split(' ').filter { it.isNotEmpty() }
                if (meta.size != META_PARTS || meta[1] != BLOB) continue
                add(TreeBlob(record.substring(tab + 1), ObjectId(meta[2]), meta[3].toLongOrNull() ?: 0))
            }
        }
    }
}

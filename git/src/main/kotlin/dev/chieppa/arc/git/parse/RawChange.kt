package dev.chieppa.arc.git.parse

import dev.chieppa.arc.core.change.ChangeStatus
import dev.chieppa.arc.core.change.ObjectId

/** One record of `git diff --raw -z`: the blobs on each side, the status and the paths. */
internal data class RawChange(
    val oldBlob: ObjectId?,
    val newBlob: ObjectId?,
    val status: ChangeStatus,
    val oldPath: String,
    val newPath: String,
)

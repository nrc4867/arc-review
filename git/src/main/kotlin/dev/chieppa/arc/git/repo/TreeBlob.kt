package dev.chieppa.arc.git.repo

import dev.chieppa.arc.core.change.ObjectId

/** One file in a tree: its [path], the [blob] holding its content and that blob's [size] in bytes. */
data class TreeBlob(val path: String, val blob: ObjectId, val size: Long)

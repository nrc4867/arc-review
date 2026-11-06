package dev.chieppa.arc.git.process

import dev.chieppa.arc.core.change.ObjectId

/** An object read from a repo: its [id], its [type] (`blob`, `tree`, `commit` or `tag`) and its bytes. */
class GitObject(val id: ObjectId, val type: String, val bytes: ByteArray)

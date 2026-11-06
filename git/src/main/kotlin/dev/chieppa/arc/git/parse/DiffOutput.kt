package dev.chieppa.arc.git.parse

import dev.chieppa.arc.core.change.ChangedFile

/** One file of a diff read by [DiffOutputParser]: the file as a change, and its section of the patch as raw bytes. */
class DiffFileOutput(val file: ChangedFile, val patch: ByteArray)

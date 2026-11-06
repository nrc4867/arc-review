package dev.chieppa.arc.core.change

/**
 * One file in a source's change. [path] is where it is after the change, or where it was for a deleted file; [oldPath]
 * is where it was before. [added] and [removed] count lines and are null for a binary file. [oldBlob] and [newBlob]
 * are the contents on each side, null on the side the file is not on.
 */
data class ChangedFile(
    val path: String,
    val status: ChangeStatus,
    val oldPath: String? = if (status == ChangeStatus.Added) null else path,
    val added: Int? = 0,
    val removed: Int? = 0,
    val oldBlob: ObjectId? = null,
    val newBlob: ObjectId? = null,
) {
    val binary: Boolean get() = added == null || removed == null

    /** The last part of [path]. */
    val name: String get() = path.substringAfterLast('/')

    /** The folder [path] is in, empty at the top. */
    val folder: String get() = path.substringBeforeLast('/', "")
}

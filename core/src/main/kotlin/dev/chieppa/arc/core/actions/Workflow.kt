package dev.chieppa.arc.core.actions

/** A workflow of a repo: its id, name and file. */
data class Workflow(val id: Long, val name: String, val path: String) {
    /** The file's name, such as `ci.yml`. */
    val fileName: String get() = path.substringAfterLast('/')
}

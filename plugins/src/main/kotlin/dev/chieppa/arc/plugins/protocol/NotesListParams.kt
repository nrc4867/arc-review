package dev.chieppa.arc.plugins.protocol

import kotlinx.serialization.Serializable

/** The person's notes, only those on the source [source] names and on file [path] when given. */
@Serializable
data class NotesListParams(val source: String? = null, val path: String? = null)

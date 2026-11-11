package dev.chieppa.arc.plugins.protocol

import kotlinx.serialization.Serializable

/** The [notes] a `notes.list` found, oldest first. */
@Serializable
data class NotesListResult(val notes: List<NoteInfo>)

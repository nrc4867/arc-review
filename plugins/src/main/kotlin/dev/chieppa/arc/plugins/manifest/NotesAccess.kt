package dev.chieppa.arc.plugins.manifest

import kotlinx.serialization.Serializable

/** Whether `app.notes` may [read] the person's notes, and [write] new ones. */
@Serializable
data class NotesAccess(val read: Boolean = false, val write: Boolean = false)

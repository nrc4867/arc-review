package dev.chieppa.arc.plugins.manifest

import kotlinx.serialization.Serializable

/** Whether `app.comments` may start a comment as a [draft], which stays local until the person stages and pushes it. */
@Serializable
data class CommentsAccess(val draft: Boolean = false)

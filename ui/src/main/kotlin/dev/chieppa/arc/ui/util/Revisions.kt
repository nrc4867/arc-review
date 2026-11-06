package dev.chieppa.arc.ui.util

import dev.chieppa.arc.core.change.ObjectId
import dev.chieppa.arc.core.pane.Revision

/** How revisions read on screen: a sha cut to its short form, a name as it is. */
object Revisions {
    private val HEX = Regex("[0-9a-f]{${ObjectId.SHA1_LENGTH},${ObjectId.SHA256_LENGTH}}")

    fun isSha(revision: Revision): Boolean = HEX.matches(revision.value)

    fun short(revision: Revision): String = if (isSha(revision)) revision.value.take(ObjectId.SHORT_LENGTH) else revision.value
}

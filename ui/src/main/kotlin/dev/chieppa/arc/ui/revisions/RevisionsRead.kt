package dev.chieppa.arc.ui.revisions

import dev.chieppa.arc.core.change.ObjectId
import dev.chieppa.arc.core.revision.RevisionHistory
import dev.chieppa.arc.host.github.pulls.CheckState

/** A source's revisions as read, with the overall [checks] on each head that has any. */
data class RevisionsRead(val history: RevisionHistory, val checks: Map<ObjectId, CheckState> = emptyMap())

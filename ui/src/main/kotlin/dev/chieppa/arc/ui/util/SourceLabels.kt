package dev.chieppa.arc.ui.util

import dev.chieppa.arc.core.change.ObjectId
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.core.source.SourceRefs

/** A source's ref as the screen shows it: its shas cut to the short form, `web@3fa9c1e..9c1e2fa`. */
object SourceLabels {
    private val LONG_SHA = Regex("[0-9a-f]{${ObjectId.SHORT_LENGTH + 1},}")

    fun of(source: Source): String = when (source) {
        is Source.Commit, is Source.CommitRange -> SourceRefs.format(source).replace(LONG_SHA) { it.value.take(ObjectId.SHORT_LENGTH) }
        else -> source.ref
    }
}

package dev.chieppa.arc.core.mark

/**
 * Decides whether a reviewed mark holds. The fingerprints' meta must match; then the replay of the reviewed change
 * decides, and where it conflicted, or there is no replay, the exact fingerprints decide.
 */
object MarkRules {
    /** The verdict on [mark] when the file's change is now [current] (null when the file left the change). */
    fun verdict(mark: FileMark, current: PatchFingerprint?, replay: ReplayOutcome? = null): MarkVerdict {
        if (current == null || current.meta != mark.fingerprint.meta) return MarkVerdict.Clear
        val exact = if (current.exact == mark.fingerprint.exact) MarkVerdict.Keep else MarkVerdict.Clear
        val usable = replay?.takeIf { it.reviewed == mark.reviewed } ?: return exact
        return when {
            mark.path in usable.conflicted -> exact
            mark.path in usable.differs -> MarkVerdict.Clear
            else -> MarkVerdict.Keep
        }
    }
}

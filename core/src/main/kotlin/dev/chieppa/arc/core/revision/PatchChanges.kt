package dev.chieppa.arc.core.revision

import dev.chieppa.arc.core.mark.PatchFingerprint

/**
 * Comparing two changes file by file through their fingerprints, which leave out context and line numbers, so a file
 * a rebase only moved counts as the same.
 */
object PatchChanges {
    /** Whether a file whose change was [before] has a different change in [after]; null means the file was not changed. */
    fun differs(before: PatchFingerprint?, after: PatchFingerprint?): Boolean = before?.exact != after?.exact

    /** The paths whose change differs between [before] and [after], by path. */
    fun differing(before: Map<String, PatchFingerprint>, after: Map<String, PatchFingerprint>): Set<String> =
        (before.keys + after.keys).filterTo(sortedSetOf()) { differs(before[it], after[it]) }
}

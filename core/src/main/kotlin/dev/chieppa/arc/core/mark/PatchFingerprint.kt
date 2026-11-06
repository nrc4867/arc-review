package dev.chieppa.arc.core.mark

/**
 * What a file's change is, for keeping a mark on it: [meta] covers its mode, rename and binary lines, [exact] its added
 * and removed lines with the meta, and [hunks] each hunk's lines alone. Context lines and line numbers are left out,
 * so a rebase that moves the change keeps the same fingerprint.
 */
data class PatchFingerprint(
    val meta: PatchHash,
    val exact: PatchHash,
    val hunks: List<PatchHash> = emptyList(),
)

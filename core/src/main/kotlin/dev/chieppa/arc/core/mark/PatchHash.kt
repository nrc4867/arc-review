package dev.chieppa.arc.core.mark

/**
 * A hash of one file's change: what a mark is held against, so a rebase that keeps the change keeps the mark and a
 * push that alters it does not.
 */
@JvmInline
value class PatchHash(val value: String) {
    init {
        require(value.isNotBlank()) { "A patch hash is not blank" }
    }

    override fun toString(): String = value
}

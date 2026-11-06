package dev.chieppa.arc.core.source

/** A commit's object name, full or abbreviated, in lowercase hex. */
@JvmInline
value class CommitSha(val value: String) {
    init {
        require(isValid(value)) { "Not a commit sha: $value" }
    }

    override fun toString(): String = value

    companion object {
        const val MIN_LENGTH = 4
        const val MAX_LENGTH = 64
        private val HEX = Regex("[0-9a-f]+")

        fun isValid(text: String): Boolean = text.length in MIN_LENGTH..MAX_LENGTH && text.matches(HEX)

        /** [text] as a sha, lowercased, or null when it is not one. */
        fun parse(text: String): CommitSha? = text.lowercase().takeIf(::isValid)?.let(::CommitSha)
    }
}

package dev.chieppa.arc.core.source

/** A branch name, held to the rules of `git check-ref-format --branch`. */
@JvmInline
value class BranchName(val value: String) {
    init {
        require(isValid(value)) { "Not a branch name: $value" }
    }

    override fun toString(): String = value

    companion object {
        private const val FORBIDDEN = "~^:?*[\\"
        private const val LAST_CONTROL = ' '
        private const val DELETE = '\u007f'
        private const val LOCK_SUFFIX = ".lock"

        fun isValid(text: String): Boolean =
            text.isNotEmpty() &&
                text.none { it <= LAST_CONTROL || it == DELETE || it in FORBIDDEN } &&
                ".." !in text &&
                "@{" !in text &&
                "//" !in text &&
                text != "@" &&
                !text.startsWith('-') &&
                !text.startsWith('/') &&
                !text.endsWith('/') &&
                !text.endsWith('.') &&
                text.split('/').none { it.startsWith('.') || it.endsWith(LOCK_SUFFIX) }

        /** [text] as a branch name, or null when git would refuse it. */
        fun parse(text: String): BranchName? = if (isValid(text)) BranchName(text) else null
    }
}

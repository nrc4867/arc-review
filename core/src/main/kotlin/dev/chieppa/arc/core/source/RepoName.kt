package dev.chieppa.arc.core.source

/**
 * A repo as a review names it: a short name such as `api`, or a host path such as `acme/api`. Letters, digits, dots,
 * dashes and underscores, with at most one slash between an owner and a name.
 */
@JvmInline
value class RepoName(val value: String) {
    init {
        require(isValid(value)) { "Not a repo name: $value" }
    }

    override fun toString(): String = value

    companion object {
        private val SEGMENT = Regex("[A-Za-z0-9._-]+")

        fun isValid(text: String): Boolean {
            val parts = text.split('/')
            return parts.size <= 2 && parts.all { it.matches(SEGMENT) && it != "." && it != ".." }
        }

        /** [text] as a repo name, or null when it is not one. */
        fun parse(text: String): RepoName? = if (isValid(text)) RepoName(text) else null
    }
}

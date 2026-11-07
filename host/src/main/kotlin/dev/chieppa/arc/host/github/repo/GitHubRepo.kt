package dev.chieppa.arc.host.github.repo

/** A repo on GitHub, by its owner and name: `nrc4867/arc-review`. Compared without regard to case, as GitHub does. */
class GitHubRepo(val owner: String, val name: String) {
    init {
        require(owner.matches(SEGMENT) && name.matches(SEGMENT)) { "Not a GitHub repo: $owner/$name" }
    }

    val fullName: String get() = "$owner/$name"

    override fun equals(other: Any?): Boolean = other is GitHubRepo && other.fullName.equals(fullName, ignoreCase = true)

    override fun hashCode(): Int = fullName.lowercase().hashCode()

    override fun toString(): String = fullName

    companion object {
        private val SEGMENT = Regex("[A-Za-z0-9._-]+")

        /** `owner/name` as a repo, or null when it is not one. */
        fun parse(text: String): GitHubRepo? {
            val parts = text.trim().split('/')
            if (parts.size != 2 || parts.any { !it.matches(SEGMENT) }) return null
            return GitHubRepo(parts[0], parts[1])
        }
    }
}

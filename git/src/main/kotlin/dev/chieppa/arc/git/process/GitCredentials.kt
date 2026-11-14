package dev.chieppa.arc.git.process

/** The GitHub token Arc's own git commands to github.com sign in with; null leaves the person's own credential helpers. */
fun interface GitCredentials {
    fun gitHubToken(): String?

    companion object {
        /** No token: git uses the person's own credential helpers. */
        val NONE: GitCredentials = GitCredentials { null }
    }
}

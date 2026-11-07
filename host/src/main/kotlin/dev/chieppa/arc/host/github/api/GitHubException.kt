package dev.chieppa.arc.host.github.api

/** A GitHub request that gave no answer to use, and why. */
class GitHubException(val reason: Reason, message: String) : Exception(message) {
    enum class Reason {
        /** No token from `gh` or the environment. */
        NoToken,

        /** GitHub refused the token. */
        Unauthorized,

        /** Any other failure: the network, a status GitHub sent, or errors in a GraphQL answer. */
        Failed,
    }
}

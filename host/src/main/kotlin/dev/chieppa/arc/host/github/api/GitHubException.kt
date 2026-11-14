package dev.chieppa.arc.host.github.api

/** A GitHub request that gave no answer to use, and why: the [status] GitHub answered with, when it answered. */
class GitHubException(val reason: Reason, message: String, val status: Int? = null) : Exception(message) {
    /** Whether GitHub gave up on the request in time (a 502 or 504), as it does for a query too heavy to answer. */
    val timedOut: Boolean get() = status == BAD_GATEWAY || status == GATEWAY_TIMEOUT

    enum class Reason {
        /** No token from `gh` or the environment. */
        NoToken,

        /** GitHub refused the token. */
        Unauthorized,

        /** Any other failure: the network, a status GitHub sent, or errors in a GraphQL answer. */
        Failed,
    }

    private companion object {
        const val BAD_GATEWAY = 502
        const val GATEWAY_TIMEOUT = 504
    }
}

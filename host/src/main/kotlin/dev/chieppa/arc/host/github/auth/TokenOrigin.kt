package dev.chieppa.arc.host.github.auth

/** Where the GitHub token in use came from. Each kind is a subtype. */
sealed interface TokenOrigin {
    /** The person's `gh` login. */
    data object GhCli : TokenOrigin

    /** The environment variable [name]. */
    data class Environment(val name: String) : TokenOrigin

    /** A source that does not say. */
    data object Other : TokenOrigin
}

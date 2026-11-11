package dev.chieppa.arc.mcp.server

/** What [RequestGuard] makes of a request: let through, or refused with an HTTP [status]. */
enum class GuardVerdict(val status: Int) {
    Allowed(200),

    /** No bearer token, or not Arc's. */
    Unauthorized(401),

    /** Sent by a web page: it carries an Origin. */
    BrowserOrigin(403),

    /** Addressed to a host name other than this machine's loopback, as a DNS rebinding page's request is. */
    WrongHost(403),
}

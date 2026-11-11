package dev.chieppa.arc.mcp.server

/** Where Arc's MCP server listens and what it calls itself. */
object McpConstants {
    /** The only address it listens on. */
    const val HOST = "127.0.0.1"

    /** The other name a local client may use for [HOST]. */
    const val LOCALHOST = "localhost"

    /** The path of its one streamable HTTP endpoint. */
    const val PATH = "/mcp"

    /** The port it listens on unless the settings name another. */
    const val DEFAULT_PORT = 47830

    /** Its name, to the client and in the command that connects Claude Code. */
    const val NAME = "arc"

    /** The keychain service its token is kept under, apart from plugins' secrets. */
    const val SECRET_SERVICE = "dev.chieppa.arc.mcp"

    /** The owner and key of its token under [SECRET_SERVICE]. */
    const val TOKEN_OWNER = "server"
    const val TOKEN_KEY = "token"

    /** How long a stopping server lets requests in flight finish, and how long it waits in all. */
    const val STOP_GRACE_MILLIS = 200L
    const val STOP_TIMEOUT_MILLIS = 1_000L

    /** What the server tells a client's model about itself when it connects. */
    const val INSTRUCTIONS = "Arc is a code review app on this machine. Its tools here help develop Arc plugins in folders " +
        "the person has added and enabled: list them, type check, reload, read logs, read and picture their views, run their " +
        "actions and send them events. They cannot push, stage, publish or mark files reviewed, and never confirm anything for the person."
}

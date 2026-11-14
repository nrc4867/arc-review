package dev.chieppa.arc.git.process

/**
 * How a git command that reaches a remote signs in to github.com with Arc's GitHub token: config that clears the
 * credential helpers for `https://github.com` and puts in their place one that answers with the token from
 * [TOKEN_VARIABLE], set only in that command's environment, so the token is never in a command line. Git runs the
 * helper through its shell on every platform.
 */
object GitAuth {
    /** The environment variable the helper reads the token from. */
    const val TOKEN_VARIABLE = "ARC_GIT_TOKEN"

    /** The user name GitHub takes with a token over HTTPS. */
    const val USER = "x-access-token"

    private const val HELPER_KEY = "credential.https://github.com.helper"
    private const val HELPER = "!f() { test \"\$1\" = get || return 0; echo username=$USER; echo \"password=\$$TOKEN_VARIABLE\"; }; f"

    /** The subcommands that reach a remote. */
    val NETWORK_COMMANDS: Set<String> = setOf("clone", "fetch", "pull", "push", "ls-remote")

    /** Config placing the token's helper before the command; the empty helper first drops every helper set before it. */
    val CONFIG: List<String> = listOf("-c", "$HELPER_KEY=", "-c", "$HELPER_KEY=$HELPER")

    /** Whether `git [args]` reaches a remote: its subcommand, after any leading `-c name=value` pairs, is one that does. */
    fun reachesRemote(args: List<String>): Boolean {
        var index = 0
        while (index < args.size && args[index] == CONFIG_FLAG) index += 2
        return args.getOrNull(index) in NETWORK_COMMANDS
    }

    /** The environment the helper needs for [token]. */
    fun environment(token: String): Map<String, String> = mapOf(TOKEN_VARIABLE to token)

    private const val CONFIG_FLAG = "-c"
}

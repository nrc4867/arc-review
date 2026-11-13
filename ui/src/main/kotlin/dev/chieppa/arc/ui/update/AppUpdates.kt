package dev.chieppa.arc.ui.update

/**
 * How the installed app updates itself, as the window reaches it: the running [version], the [previous] one it can
 * switch back to, finding a newer release, readying it, and making it or the previous one start next.
 */
interface AppUpdates {
    val version: String

    /** The version Settings can switch back to, or null. */
    val previous: String?

    /** A newer release's version, or null when there is none. Throws when GitHub cannot be asked. */
    suspend fun check(): String?

    /** Downloads, checks and readies the release [check] found, reporting [progress] (bytes done, total when known). Throws when it cannot. */
    suspend fun prepare(progress: (Long, Long?) -> Unit)

    /** Makes the readied version the one that starts and has Arc start it once this window closes. */
    suspend fun activate()

    /** Makes the previous version the one that starts and has Arc start it once this window closes. */
    suspend fun switchBack()
}

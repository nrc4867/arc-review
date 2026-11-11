package dev.chieppa.arc.plugins.protocol

import kotlinx.serialization.Serializable

/**
 * Start [command], a program and its arguments with no shell between, in the folder [cwd] names: [WORKTREE] for the
 * clone of [context]'s source, [PLUGIN] for the plugin's own folder, or an absolute path.
 */
@Serializable
data class ConsoleRunParams(val command: List<String>, val cwd: String = WORKTREE, val context: CallContext = CallContext()) {
    companion object {
        const val WORKTREE = "worktree"
        const val PLUGIN = "plugin"
    }
}

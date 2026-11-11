package dev.chieppa.arc.plugins.manifest

/** One thing a plugin may do, as Arc lists it on the plugin's row and checks calls against. */
sealed interface Capability {
    data object Panels : Capability
    data object Panes : Capability
    data object Actions : Capability
    data object Console : Capability
    data object Git : Capability

    /** Reading the person's notes. */
    data object ReadNotes : Capability

    /** Adding notes. */
    data object WriteNotes : Capability

    /** Starting comment drafts, which only the person stages and pushes. */
    data object DraftComments : Capability

    /** Reading files under [path]. */
    data class ReadFiles(val path: String) : Capability

    /** Writing files under [path]. */
    data class WriteFiles(val path: String) : Capability

    /** Talking to [host] over the network. */
    data class Network(val host: String) : Capability

    /** Calling the MCP server the person maps to [role]. */
    data class Mcp(val role: String) : Capability

    companion object {
        /** The capabilities [capabilities] declares that [approved] does not. */
        fun added(capabilities: Capabilities, approved: Capabilities): List<Capability> = of(capabilities) - of(approved).toSet()

        /** Every capability [capabilities] declares, in the order a plugin's row lists them. */
        fun of(capabilities: Capabilities): List<Capability> = buildList {
            if (capabilities.panels) add(Panels)
            if (capabilities.panes) add(Panes)
            if (capabilities.actions) add(Actions)
            if (capabilities.console) add(Console)
            if (capabilities.git) add(Git)
            if (capabilities.notes.read) add(ReadNotes)
            if (capabilities.notes.write) add(WriteNotes)
            if (capabilities.comments.draft) add(DraftComments)
            capabilities.files.read.forEach { add(ReadFiles(it)) }
            capabilities.files.write.forEach { add(WriteFiles(it)) }
            capabilities.network.forEach { add(Network(it)) }
            capabilities.mcp.forEach { add(Mcp(it)) }
        }
    }
}

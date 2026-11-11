package dev.chieppa.arc.plugins.manifest

/** One thing a plugin may do, as Arc lists it on the plugin's row and checks calls against. */
sealed interface Capability {
    data object Panels : Capability
    data object Panes : Capability
    data object Actions : Capability
    data object Console : Capability
    data object Git : Capability
    data object Processes : Capability

    /** Reading files under [path]. */
    data class ReadFiles(val path: String) : Capability

    /** Writing files under [path]. */
    data class WriteFiles(val path: String) : Capability

    /** Talking to [host] over the network. */
    data class Network(val host: String) : Capability

    /** Calling the MCP server the person maps to [role]. */
    data class Mcp(val role: String) : Capability

    companion object {
        /** Every capability [capabilities] declares, in the order a plugin's row lists them. */
        fun of(capabilities: Capabilities): List<Capability> = buildList {
            if (capabilities.panels) add(Panels)
            if (capabilities.panes) add(Panes)
            if (capabilities.actions) add(Actions)
            if (capabilities.console) add(Console)
            if (capabilities.git) add(Git)
            capabilities.files.read.forEach { add(ReadFiles(it)) }
            capabilities.files.write.forEach { add(WriteFiles(it)) }
            capabilities.network.forEach { add(Network(it)) }
            if (capabilities.processes) add(Processes)
            capabilities.mcp.forEach { add(Mcp(it)) }
        }
    }
}

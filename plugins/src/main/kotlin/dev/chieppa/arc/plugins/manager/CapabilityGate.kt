package dev.chieppa.arc.plugins.manager

import dev.chieppa.arc.plugins.manifest.Capabilities
import dev.chieppa.arc.plugins.protocol.Contributions
import dev.chieppa.arc.plugins.protocol.ErrorCodes
import dev.chieppa.arc.plugins.protocol.Methods
import dev.chieppa.arc.plugins.protocol.RpcError
import dev.chieppa.arc.plugins.protocol.RpcException

/** Checks a plugin's calls and contributions against the capabilities its manifest declares. */
object CapabilityGate {
    private val NEEDS: Map<String, (Capabilities) -> Boolean> = mapOf(
        Methods.CONSOLE_RUN to Capabilities::console,
        Methods.CONSOLE_WRITE to Capabilities::console,
        Methods.CONSOLE_KILL to Capabilities::console,
        Methods.PANES_OPEN to Capabilities::panes,
        Methods.GIT_GREP to Capabilities::git,
    )
    private const val UNDECLARED = "The plugin's manifest does not declare what"

    /** Throws [RpcException] with [ErrorCodes.DENIED] unless [capabilities] allow calling [method]. */
    fun check(method: String, capabilities: Capabilities) {
        val allowed = NEEDS[method] ?: return
        if (!allowed(capabilities)) throw RpcException(RpcError(ErrorCodes.DENIED, "$UNDECLARED $method needs"))
    }

    /** [contributions] without the kinds [capabilities] do not declare. */
    fun allowed(contributions: Contributions, capabilities: Capabilities): Contributions = contributions.copy(
        actions = if (capabilities.actions) contributions.actions else emptyList(),
        panels = if (capabilities.panels) contributions.panels else emptyList(),
        panes = if (capabilities.panes) contributions.panes else emptyList(),
    )
}

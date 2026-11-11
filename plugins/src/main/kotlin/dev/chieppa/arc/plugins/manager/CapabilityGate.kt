package dev.chieppa.arc.plugins.manager

import dev.chieppa.arc.plugins.manifest.Capabilities
import dev.chieppa.arc.plugins.protocol.AppEvents
import dev.chieppa.arc.plugins.protocol.Contributions
import dev.chieppa.arc.plugins.protocol.ErrorCodes
import dev.chieppa.arc.plugins.protocol.Methods
import dev.chieppa.arc.plugins.protocol.RpcError
import dev.chieppa.arc.plugins.protocol.RpcException
import java.net.URI
import java.net.URISyntaxException

/** Checks a plugin's calls and contributions against the capabilities its manifest declares. */
object CapabilityGate {
    private val NEEDS: Map<String, (Capabilities) -> Boolean> = mapOf(
        Methods.CONSOLE_RUN to Capabilities::console,
        Methods.CONSOLE_WRITE to Capabilities::console,
        Methods.CONSOLE_KILL to Capabilities::console,
        Methods.PANES_OPEN to Capabilities::panes,
        Methods.GIT_GREP to Capabilities::git,
        Methods.FILES_READ to { it.files.read.isNotEmpty() },
        Methods.FILES_WRITE to { it.files.write.isNotEmpty() },
        Methods.NET_FETCH to { it.network.isNotEmpty() },
        Methods.NOTES_LIST to { it.notes.read },
        Methods.NOTES_CREATE to { it.notes.write },
        Methods.COMMENTS_DRAFT to { it.comments.draft },
    )
    private val EVENT_NEEDS: Map<String, (Capabilities) -> Boolean> = mapOf(
        AppEvents.NOTES_CHANGED to { it.notes.read },
    )
    private val SCHEMES = setOf("https", "http")
    private const val WILDCARD = "*."
    private const val UNDECLARED = "The plugin's manifest does not declare what"
    private const val UNDECLARED_HOST = "The plugin's manifest does not name the host of"

    /** Throws [RpcException] with [ErrorCodes.DENIED] unless [capabilities] allow calling [method]. */
    fun check(method: String, capabilities: Capabilities) {
        val allowed = NEEDS[method] ?: return
        if (!allowed(capabilities)) throw denied("$UNDECLARED $method needs")
    }

    /** Whether a plugin with [capabilities] is told of event [name]. */
    fun hears(name: String, capabilities: Capabilities): Boolean = EVENT_NEEDS[name]?.invoke(capabilities) ?: true

    /** Throws [RpcException] with [ErrorCodes.DENIED] unless [url] is http or https to a host [capabilities] name. */
    fun checkHost(url: String, capabilities: Capabilities) {
        if (!allowsHost(url, capabilities)) throw denied("$UNDECLARED_HOST $url")
    }

    /**
     * Whether [url] goes to a host the manifest's network list names: the host itself, or under it when the entry is
     * `*.host`.
     */
    fun allowsHost(url: String, capabilities: Capabilities): Boolean {
        val uri = try {
            URI(url)
        } catch (_: URISyntaxException) {
            return false
        }
        val host = uri.host?.lowercase() ?: return false
        if (uri.scheme?.lowercase() !in SCHEMES || uri.userInfo != null) return false
        return capabilities.network.map { it.lowercase() }.any { declared ->
            if (declared.startsWith(WILDCARD)) host.endsWith(declared.substring(1)) else host == declared
        }
    }

    /** [contributions] without the kinds [capabilities] do not declare. */
    fun allowed(contributions: Contributions, capabilities: Capabilities): Contributions = contributions.copy(
        actions = if (capabilities.actions) contributions.actions else emptyList(),
        panels = if (capabilities.panels) contributions.panels else emptyList(),
        panes = if (capabilities.panes) contributions.panes else emptyList(),
    )

    private fun denied(message: String) = RpcException(RpcError(ErrorCodes.DENIED, message))
}

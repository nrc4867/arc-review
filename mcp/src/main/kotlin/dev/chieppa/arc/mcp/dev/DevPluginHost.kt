package dev.chieppa.arc.mcp.dev

import dev.chieppa.arc.plugins.host.PluginDiagnostic
import dev.chieppa.arc.plugins.protocol.AppEventParams
import dev.chieppa.arc.plugins.protocol.CallContext

/** The plugins as the development tools reach them; the tools decide which plugins they may. */
interface DevPluginHost {
    /** Every plugin Arc knows of, in source order. */
    fun plugins(): List<DevPlugin>

    /** Type checks plugin [id] now and gives every error. */
    suspend fun typeCheck(id: String): List<PluginDiagnostic>

    /** Starts plugin [id] again from its files and gives it once it runs or has failed. */
    suspend fun reload(id: String): DevPlugin?

    /** The lines plugin [id]'s log holds, oldest first. */
    fun log(id: String): List<String>

    /** The views of plugin [id] Arc shows now. */
    fun views(id: String): List<DevView>

    /** A view of plugin [id]'s [contribution] once Arc shows it and it has drawn, waiting a while; null when none comes. */
    suspend fun awaitView(id: String, contribution: String): DevView?

    /** Runs plugin [id]'s action [action] on [context], returning once it has; throws when it fails. */
    suspend fun runAction(id: String, action: String, context: CallContext)

    /** Tells plugin [id], and only it, that [event] happened; false when its capabilities keep it from hearing it. */
    fun emit(id: String, event: AppEventParams): Boolean
}

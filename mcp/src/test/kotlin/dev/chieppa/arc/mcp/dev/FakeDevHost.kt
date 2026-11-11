package dev.chieppa.arc.mcp.dev

import dev.chieppa.arc.plugins.host.PluginDiagnostic
import dev.chieppa.arc.plugins.protocol.AppEventParams
import dev.chieppa.arc.plugins.protocol.CallContext
import kotlinx.coroutines.CompletableDeferred

/**
 * Plugins as a test sets them out in [plugins], with each one's [errors], [logs] and [views]. Actions run as [action]
 * says, events a plugin hears are kept in [events], and every call that reaches a plugin is kept in [reached].
 */
class FakeDevHost(var plugins: List<DevPlugin>) : DevPluginHost {
    val errors = mutableMapOf<String, List<PluginDiagnostic>>()
    val logs = mutableMapOf<String, List<String>>()
    val views = mutableMapOf<String, List<DevView>>()
    val opened = mutableMapOf<String, DevView>()
    val events = mutableListOf<Pair<String, AppEventParams>>()
    val deaf = mutableSetOf<String>()
    val reached = mutableListOf<String>()
    var action: suspend (String) -> Unit = {}

    /** An action that never returns, as one waiting on the person does. */
    val waiting: suspend (String) -> Unit = { CompletableDeferred<Unit>().await() }

    override fun plugins(): List<DevPlugin> = plugins

    override suspend fun typeCheck(id: String): List<PluginDiagnostic> = errors[id].orEmpty().also { reached += id }

    override suspend fun reload(id: String): DevPlugin? = plugins.firstOrNull { it.id == id }.also { reached += id }

    override fun log(id: String): List<String> = logs[id].orEmpty().also { reached += id }

    override fun views(id: String): List<DevView> = views[id].orEmpty()

    override suspend fun awaitView(id: String, contribution: String): DevView? = opened[contribution]

    override suspend fun runAction(id: String, action: String, context: CallContext) {
        reached += id
        this.action(action)
    }

    override fun emit(id: String, event: AppEventParams): Boolean {
        reached += id
        if (event.name in deaf) return false
        events += id to event
        return true
    }
}

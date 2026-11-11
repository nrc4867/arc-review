package dev.chieppa.arc.mcp.dev

import dev.chieppa.arc.plugins.config.PluginSourceSpec
import dev.chieppa.arc.plugins.host.PluginDiagnostic
import dev.chieppa.arc.plugins.host.PluginStatus
import dev.chieppa.arc.plugins.manager.CapabilityGate
import dev.chieppa.arc.plugins.manager.PluginEntry
import dev.chieppa.arc.plugins.manager.PluginInstance
import dev.chieppa.arc.plugins.manager.PluginManager
import dev.chieppa.arc.plugins.manager.PluginView
import dev.chieppa.arc.plugins.manifest.Capability
import dev.chieppa.arc.plugins.protocol.AppEventParams
import dev.chieppa.arc.plugins.protocol.CallContext
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withTimeoutOrNull

/** The plugins [manager] runs, as the development tools reach them, waiting as [timings] say. */
class ManagerPluginHost(private val manager: PluginManager, private val timings: DevTimings = DevTimings()) : DevPluginHost {
    override fun plugins(): List<DevPlugin> = manager.entries.value.map(::describe)

    override suspend fun typeCheck(id: String): List<PluginDiagnostic> = manager.typeCheck(id).orEmpty()

    override suspend fun reload(id: String): DevPlugin? {
        val instance = manager.instance(id) ?: return null
        manager.reload(id)
        withTimeoutOrNull(timings.reloadMillis) { instance.status.first { it !is PluginStatus.Starting } } ?: return null
        return manager.entries.value.firstOrNull { it.id == id }?.let(::describe)
    }

    override fun log(id: String): List<String> = manager.instance(id)?.log?.lines?.value.orEmpty()

    override fun views(id: String): List<DevView> = manager.instance(id)?.views().orEmpty().map(::describe)

    override suspend fun awaitView(id: String, contribution: String): DevView? {
        val instance = manager.instance(id) ?: return null
        return withTimeoutOrNull(timings.drawMillis) {
            val view = instance.opened.map { views -> views.firstOrNull { it.contribution == contribution } }.filterNotNull().first()
            view.nodes.first { it.isNotEmpty() }
            describe(view)
        }
    }

    override suspend fun runAction(id: String, action: String, context: CallContext) {
        manager.instance(id)?.run(action, context)
    }

    override fun emit(id: String, event: AppEventParams): Boolean {
        val instance = manager.instance(id) ?: return false
        if (!CapabilityGate.hears(event.name, instance.manifest.capabilities)) return false
        instance.emit(event)
        return true
    }

    private fun describe(view: PluginView): DevView = DevView(view.id, view.kind, view.contribution, view.nodes.value)

    private fun describe(entry: PluginEntry): DevPlugin {
        val instance = entry.instance
        val status = instance?.status?.value
        val types = manager.typeErrors.value[entry.id]
        val failed = (status as? PluginStatus.Failed)?.diagnostics.orEmpty()
        val contributions = instance?.contributions?.value
        return DevPlugin(
            id = entry.id,
            title = entry.found.manifest?.title ?: entry.id,
            folder = entry.found.folder.toString(),
            directory = entry.found.source is PluginSourceSpec.Directory,
            enabled = entry.enabled,
            state = state(entry, instance, status),
            problems = (listOfNotNull(entry.found.diagnostic) + failed + types?.shown.orEmpty()).map(DevProblem::of),
            typeErrors = types?.total ?: 0,
            asks = entry.asks.map(::label),
            logLines = instance?.log?.lines?.value?.size ?: 0,
            actions = contributions?.actions.orEmpty().map { it.id },
            panels = contributions?.panels.orEmpty().map { it.id },
            panes = contributions?.panes.orEmpty().map { it.id },
        )
    }

    private fun state(entry: PluginEntry, instance: PluginInstance?, status: PluginStatus?): DevPluginState = when {
        entry.found.manifest == null -> DevPluginState.Unreadable
        !entry.enabled -> DevPluginState.Off
        entry.asks.isNotEmpty() -> DevPluginState.WaitingForApproval
        instance == null || status == null || status == PluginStatus.Off -> DevPluginState.Off
        status == PluginStatus.Starting -> DevPluginState.Starting
        status is PluginStatus.Running -> DevPluginState.Running
        status is PluginStatus.Restarting -> DevPluginState.Restarting
        else -> DevPluginState.Failed
    }

    private fun label(capability: Capability): String = when (capability) {
        is Capability.ReadFiles -> "files.read ${capability.path}"
        is Capability.WriteFiles -> "files.write ${capability.path}"
        is Capability.Network -> "network ${capability.host}"
        is Capability.Mcp -> "mcp ${capability.role}"
        else -> capability.toString()
    }
}

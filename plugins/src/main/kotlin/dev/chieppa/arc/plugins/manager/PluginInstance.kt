package dev.chieppa.arc.plugins.manager

import dev.chieppa.arc.plugins.host.PluginLog
import dev.chieppa.arc.plugins.host.PluginStatus
import dev.chieppa.arc.plugins.host.PluginSupervisor
import dev.chieppa.arc.plugins.host.RpcHandler
import dev.chieppa.arc.plugins.manifest.PluginManifest
import dev.chieppa.arc.plugins.protocol.ActionRunParams
import dev.chieppa.arc.plugins.protocol.AppEventParams
import dev.chieppa.arc.plugins.protocol.CallContext
import dev.chieppa.arc.plugins.protocol.Contributions
import dev.chieppa.arc.plugins.protocol.Methods
import dev.chieppa.arc.plugins.protocol.MountParams
import dev.chieppa.arc.plugins.protocol.PatchParams
import dev.chieppa.arc.plugins.protocol.ProtocolJson
import dev.chieppa.arc.plugins.protocol.SettingsChangedParams
import dev.chieppa.arc.plugins.protocol.UnmountParams
import dev.chieppa.arc.plugins.protocol.ViewEventParams
import dev.chieppa.arc.plugins.protocol.ViewKind
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

/**
 * One enabled plugin: its worker kept running by a [PluginSupervisor], the views Arc shows of it, its actions and
 * events, and its calls into Arc. Its [manifest], [folder] and the [root] its source reads from change when its files
 * are read again.
 */
class PluginInstance internal constructor(
    @Volatile var manifest: PluginManifest,
    @Volatile var folder: Path,
    @Volatile var root: Path,
    private val services: PluginServices,
    private val settingsOf: () -> JsonObject,
    setSettings: (JsonObject) -> Unit,
) : RpcHandler {
    private val viewIds = AtomicLong()
    private val views = ConcurrentHashMap<String, PluginView>()
    private val open = MutableStateFlow<List<PluginView>>(emptyList())
    private val calls = PluginCalls({ manifest }, { folder }, services, settingsOf, setSettings) { method, params -> supervisor.notify(method, params) }

    val id: String get() = manifest.id
    val log = PluginLog()

    private val supervisor = PluginSupervisor(services.scope, services.workers, this, log, services.timings, services.clock, ::mountAll) {
        services.preparer.prepare(folder, root, manifest, settingsOf())
    }

    val status: StateFlow<PluginStatus> get() = supervisor.status

    /** What it contributes while it runs, limited to what its manifest declares; nothing otherwise. */
    val contributions: StateFlow<Contributions> = supervisor.status
        .map { (it as? PluginStatus.Running)?.contributions?.let { c -> CapabilityGate.allowed(c, manifest.capabilities) } ?: Contributions() }
        .stateIn(services.scope, SharingStarted.Eagerly, Contributions())

    /** Starts it, or starts it again from its files. */
    fun start() = supervisor.start()

    fun stop() {
        supervisor.stop()
        services.consoles.stopAll(id)
        views.values.forEach(PluginView::reset)
    }

    /** Runs its action [action] on [context]; failures go to its log. */
    fun runAction(action: String, context: CallContext) {
        services.scope.launch { runCatching { run(action, context) } }
    }

    /**
     * Runs its action [action] on [context] and returns once the action has; a failure goes to its log and is thrown,
     * as [dev.chieppa.arc.plugins.protocol.RpcException] or [dev.chieppa.arc.plugins.host.PluginGoneException].
     */
    suspend fun run(action: String, context: CallContext) {
        try {
            supervisor.request(Methods.ACTION_RUN, ProtocolJson.toElement(ActionRunParams.serializer(), ActionRunParams(action, context)))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            log.add("$action: ${e.message}")
            throw e
        }
    }

    /** The views Arc shows of it now: its open panels, panes and settings page. */
    fun views(): List<PluginView> = open.value

    /** The views Arc shows of it, as they open and close. */
    val opened: StateFlow<List<PluginView>> get() = open

    /** Shows its [contribution] of [kind] on [context] with [props]; the view draws once the plugin runs. */
    fun mount(kind: ViewKind, contribution: String, context: CallContext, props: JsonObject = JsonObject(emptyMap())): PluginView {
        val view = PluginView("$id/${viewIds.incrementAndGet()}", kind, contribution, context, props, ::event, ::unmount)
        views[view.id] = view
        open.value = views.values.toList()
        if (supervisor.status.value is PluginStatus.Running) services.scope.launch { mountOne(view) }
        return view
    }

    /** Tells it its settings changed; its open views redraw. */
    fun settingsChanged() {
        services.scope.launch { supervisor.notify(Methods.SETTINGS_CHANGED, ProtocolJson.toElement(SettingsChangedParams.serializer(), SettingsChangedParams(settingsOf()))) }
    }

    /** Tells it [event] happened, when its capabilities let it hear of it. */
    fun emit(event: AppEventParams) {
        if (supervisor.status.value !is PluginStatus.Running || !CapabilityGate.hears(event.name, manifest.capabilities)) return
        services.scope.launch { supervisor.notify(Methods.EVENT, ProtocolJson.toElement(AppEventParams.serializer(), event)) }
    }

    override suspend fun request(method: String, params: JsonElement): JsonElement = calls.answer(method, params)

    override fun notification(method: String, params: JsonElement) {
        if (method != Methods.VIEW_PATCH) return
        val patch = runCatching { ProtocolJson.fromElement(PatchParams.serializer(), params) }.getOrNull() ?: return
        views[patch.view]?.apply(patch.ops)
    }

    private suspend fun mountAll() {
        views.values.forEach { mountOne(it) }
    }

    private suspend fun mountOne(view: PluginView) {
        view.reset()
        val params = MountParams(view.id, view.kind, view.contribution, view.context, view.props)
        runCatching { supervisor.request(Methods.VIEW_MOUNT, ProtocolJson.toElement(MountParams.serializer(), params)) }
            .onFailure { log.add("${view.contribution}: ${it.message}") }
    }

    private fun event(view: PluginView, node: Int, handler: String, args: JsonArray) {
        services.scope.launch {
            supervisor.notify(Methods.VIEW_EVENT, ProtocolJson.toElement(ViewEventParams.serializer(), ViewEventParams(view.id, node, handler, args)))
        }
    }

    private fun unmount(view: PluginView) {
        if (views.remove(view.id) == null) return
        open.value = views.values.toList()
        services.scope.launch { supervisor.notify(Methods.VIEW_UNMOUNT, ProtocolJson.toElement(UnmountParams.serializer(), UnmountParams(view.id))) }
    }
}

package dev.chieppa.arc.ui.plugins

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.chieppa.arc.core.pane.Page
import dev.chieppa.arc.core.source.SourceRefs
import dev.chieppa.stretch.frame.TabItem
import dev.chieppa.arc.plugins.config.PluginsConfig
import dev.chieppa.arc.plugins.host.PluginStatus
import dev.chieppa.arc.plugins.manager.CapabilityGate
import dev.chieppa.arc.plugins.manager.PluginEntry
import dev.chieppa.arc.plugins.manager.PluginManager
import dev.chieppa.arc.plugins.manager.PluginUi
import dev.chieppa.arc.plugins.notes.PluginNotes
import dev.chieppa.arc.plugins.protocol.PanesOpenParams
import dev.chieppa.arc.plugins.typecheck.TypeErrors
import dev.chieppa.arc.plugins.protocol.AppEventParams
import dev.chieppa.arc.plugins.protocol.Contributions
import dev.chieppa.arc.ui.plugins.notes.WindowNotes
import dev.chieppa.arc.ui.workbench.Workbench
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * The window's side of plugins: the [manager] that runs them (none where plugins are off), what each one is and
 * contributes as Compose state, its actions in the window's registry, and what plugins ask of the window: opening
 * their panes, and the person's notes.
 */
class PluginsController(private val workbench: Workbench, makeManager: (PluginUi) -> PluginManager?) {
    val manager: PluginManager? = makeManager(
        object : PluginUi {
            override suspend fun openPane(plugin: String, params: PanesOpenParams) {
                withContext(workbench.dispatchers.main) {
                    val source = params.context.source?.ref?.let(SourceRefs::parse)
                    workbench.panes.openPluginPane(plugin, params.pane, params.props.toString(), source)
                }
            }

            override val notes: PluginNotes = WindowNotes(workbench)
        },
    )

    /** Every plugin found, in source order. */
    var entries by mutableStateOf<List<PluginEntry>>(emptyList())
        private set

    /** The plugins' setup: sources, which are enabled, their settings. */
    var config by mutableStateOf(PluginsConfig())
        private set

    /** Each enabled plugin's status, by id. */
    var statuses by mutableStateOf<Map<String, PluginStatus>>(emptyMap())
        private set

    /** Each running directory plugin's type errors, by id. */
    var typeErrors by mutableStateOf<Map<String, TypeErrors>>(emptyMap())
        private set

    val actions = PluginActions(workbench)

    /** Reads the plugins' setup, starts those enabled and follows them. */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun start() {
        val manager = manager ?: return
        manager.start()
        workbench.scope.launch { manager.config.collect { config = it } }
        workbench.scope.launch { manager.typeErrors.collect { typeErrors = it } }
        workbench.scope.launch {
            manager.entries.flatMapLatest { list ->
                entries = list
                val running = list.mapNotNull { it.instance }
                if (running.isEmpty()) flowOf(emptyMap()) else combine(running.map { i -> i.status.map { i.id to it } }) { it.toMap() }
            }.collect {
                statuses = it
                actions.sync(entries, ::contributions)
            }
        }
    }

    /** What plugin [id] contributes while it runs, within what its manifest declares. */
    fun contributions(id: String): Contributions {
        val running = statuses[id] as? PluginStatus.Running ?: return Contributions()
        val manifest = entries.firstOrNull { it.id == id }?.found?.manifest ?: return Contributions()
        return CapabilityGate.allowed(running.contributions, manifest.capabilities)
    }

    /** Running plugins with what they contribute, in source order. */
    val running: List<Pair<PluginEntry, Contributions>>
        get() = entries.filter { statuses[it.id] is PluginStatus.Running }.map { it to contributions(it.id) }

    /** The tab of plugin pane [page]: its pane kind's title and icon, or its kind's id while the plugin is off. */
    fun tabOf(page: Page.Plugin): TabItem {
        val pane = contributions(page.plugin).panes.firstOrNull { it.id == page.pane }
        return TabItem(page.id.value, pane?.title ?: page.pane, PluginIconNames.orPlugin(pane?.icon))
    }

    /** Tells every running plugin [event] happened. */
    fun emit(event: AppEventParams) {
        manager?.emit(event)
    }

    fun stop() {
        manager?.stop()
    }
}

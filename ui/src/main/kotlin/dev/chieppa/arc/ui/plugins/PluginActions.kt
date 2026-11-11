package dev.chieppa.arc.ui.plugins

import androidx.compose.runtime.mutableStateMapOf
import dev.chieppa.arc.plugins.manager.PluginEntry
import dev.chieppa.arc.plugins.protocol.ActionContribution
import dev.chieppa.arc.plugins.protocol.Contributions
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.core.Action
import dev.chieppa.stretch.core.ActionId

/**
 * Plugins' actions in the window's action registry, so they run from the palette, menus and keys like Arc's own.
 * Each is registered the first time its plugin offers it and stays registered, shown only while its plugin offers
 * it; it runs on the focused source and file. Its label, from the plugin, is its text.
 */
class PluginActions(private val workbench: Workbench) {
    private val offered = mutableStateMapOf<ActionId, Pair<String, ActionContribution>>()

    /** The label of plugin action [key], or null when it is not one. */
    fun label(key: String): String? = offered[ActionId(key)]?.second?.label

    /** The icon name of plugin action [key], or null. */
    fun icon(key: String): String? = offered[ActionId(key)]?.second?.icon

    /** Follows what [entries] offer now, as [contributions] gives each one's. */
    fun sync(entries: List<PluginEntry>, contributions: (String) -> Contributions) {
        val now = entries.flatMap { entry -> contributions(entry.id).actions.map { PluginIds.action(entry.id, it.id) to (entry.id to it) } }.toMap()
        (offered.keys - now.keys).forEach(offered::remove)
        now.forEach { (id, offer) ->
            offered[id] = offer
            if (workbench.registry[id] == null) register(id, offer.second)
        }
    }

    private fun register(id: ActionId, action: ActionContribution) {
        workbench.registry.register(
            Action(
                id,
                id.value,
                id.value,
                listOfNotNull(PluginIds.keys(action.keys)),
                isAvailable = { id in offered },
                run = { run(id) },
            ),
        )
    }

    private fun run(id: ActionId) {
        val (plugin, action) = offered[id] ?: return
        workbench.plugins.manager?.instance(plugin)?.runAction(action.id, PluginContexts.focused(workbench))
    }
}

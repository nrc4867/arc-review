package dev.chieppa.arc.ui.plugins

import dev.chieppa.stretch.core.ActionId
import dev.chieppa.stretch.core.KeyChord

/** How a plugin's contributions are named in Arc's registries, so they never clash with Arc's own or each other's. */
object PluginIds {
    private const val PREFIX = "plugin."
    private const val SEPARATOR = "."

    /** The action id of plugin [plugin]'s action [action]. */
    fun action(plugin: String, action: String): ActionId = ActionId("$PREFIX$plugin$SEPARATOR$action")

    /** The drawer id of plugin [plugin]'s panel [panel]. */
    fun drawer(plugin: String, panel: String): String = "$PREFIX$plugin$SEPARATOR$panel"

    /** Whether [id] is a plugin's drawer or action. */
    fun isPlugin(id: String): Boolean = id.startsWith(PREFIX)

    /** The settings card of plugin [plugin]'s own page. */
    fun settingsCard(plugin: String): String = "${PREFIX}settings$SEPARATOR$plugin"

    /** The plugin whose own settings page card [card] is, or null for another card. */
    fun settingsPluginOf(card: String): String? = card.removePrefix("${PREFIX}settings$SEPARATOR").takeIf { it != card }

    /** The chord a plugin writes as `Ctrl+Shift+T`, or null when it is not one. */
    fun keys(text: String?): KeyChord? = text?.let { runCatching { KeyChord.parse(it.replace('+', ' ')) }.getOrNull() }
}

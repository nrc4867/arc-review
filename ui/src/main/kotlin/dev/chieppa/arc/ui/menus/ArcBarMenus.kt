package dev.chieppa.arc.ui.menus

import androidx.compose.runtime.Composable
import dev.chieppa.arc.ui.actions.actionIcon
import dev.chieppa.stretch.core.ActionRegistry
import dev.chieppa.stretch.core.Keymap
import dev.chieppa.stretch.frame.ActionMenuItem
import dev.chieppa.stretch.frame.BarMenu
import dev.chieppa.stretch.frame.BarMenus
import dev.chieppa.stretch.frame.MenuDivider
import org.jetbrains.compose.resources.stringResource

/** [ArcMenus] in the top bar, for Windows and Linux. */
@Composable
fun ArcBarMenus(registry: ActionRegistry, keymap: Keymap) {
    val menus = ArcMenus.all.map { menu ->
        BarMenu(stringResource(menu.title)) { close ->
            for (id in menu.items) {
                if (id == null) MenuDivider() else registry[id]?.let { ActionMenuItem(it, keymap, ::actionIcon, close) }
            }
        }
    }
    BarMenus(menus)
}

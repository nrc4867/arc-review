package dev.chieppa.arc.ui.menus

import androidx.compose.runtime.Composable
import dev.chieppa.arc.ui.actions.ArcActionIds
import dev.chieppa.stretch.frame.AppMenu
import org.jetbrains.compose.resources.stringResource

/** What the macOS app menu already holds, so the other menus leave it out. */
private val IN_APP_MENU = setOf(ArcActionIds.SETTINGS, ArcActionIds.QUIT)

/** [ArcMenus] for the macOS menu bar, whose app menu holds Settings and Quit. */
@Composable
fun appMenus(): List<AppMenu> = ArcMenus.all
    .map { it.without(IN_APP_MENU) }
    .filter { menu -> menu.items.any { it != null } }
    .map { AppMenu(stringResource(it.title), it.items) }

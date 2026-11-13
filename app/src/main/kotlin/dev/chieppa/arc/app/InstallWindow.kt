package dev.chieppa.arc.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.window.rememberWindowState
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.install_title
import dev.chieppa.arc.install.InstallListener
import dev.chieppa.arc.ui.install.InstallController
import dev.chieppa.arc.ui.install.InstallRows
import dev.chieppa.arc.ui.install.InstallView
import dev.chieppa.arc.ui.settings.UiPreferences
import dev.chieppa.arc.ui.util.ArcDispatchers
import dev.chieppa.stretch.frame.StretchWindow
import dev.chieppa.stretch.theme.StretchTheme
import org.jetbrains.compose.resources.stringResource

/**
 * The window that installs Arc from the jar it runs from: it starts at once with [install], and Open Arc runs
 * [onOpen]; closing it runs [onClose].
 */
@Composable
fun InstallWindow(version: String, install: suspend (InstallListener) -> Unit, onOpen: () -> Unit, onClose: () -> Unit) {
    StretchWindow(
        onCloseRequest = onClose,
        title = stringResource(Res.string.install_title),
        state = rememberWindowState(width = AppConstants.INSTALL_WIDTH, height = AppConstants.INSTALL_HEIGHT),
    ) {
        val scope = rememberCoroutineScope()
        val controller = remember { InstallController(install, scope, ArcDispatchers.Real) }
        LaunchedEffect(controller) { controller.start() }
        StretchTheme(UiPreferences().theme) {
            InstallView(controller, remember { InstallRows(version) }, onOpen)
        }
    }
}

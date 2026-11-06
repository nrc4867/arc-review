package dev.chieppa.arc.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.rememberWindowState
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.app_name
import dev.chieppa.arc.design.generated.review_default_name
import dev.chieppa.arc.ui.actions.actionText
import dev.chieppa.arc.ui.frame.ArcFrame
import dev.chieppa.arc.ui.menus.appMenus
import dev.chieppa.arc.ui.settings.UiPreferences
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.frame.LocalActionText
import dev.chieppa.stretch.frame.MacMenuBar
import dev.chieppa.stretch.frame.StretchWindow
import dev.chieppa.stretch.theme.StretchTheme
import kotlinx.coroutines.cancel
import org.jetbrains.compose.resources.stringResource

/**
 * Arc's main window on [session]'s saved state, opening with the [initial] look and the last review. Each change to
 * the look goes to [onPreferencesChange], and the review is saved as it changes and when it is left. [onExit] ends the
 * app.
 */
@Composable
fun ArcWindow(session: AppSession, initial: UiPreferences, onPreferencesChange: suspend (UiPreferences) -> Unit, onExit: () -> Unit) {
    val exit by rememberUpdatedState(onExit)
    val defaultName = stringResource(Res.string.review_default_name)
    var reviewId by remember { mutableStateOf<String?>(null) }
    var preferences by remember { mutableStateOf(initial) }
    var current by remember { mutableStateOf<Workbench?>(null) }
    fun save(workbench: Workbench) = session.save(workbench.state, workbench.treeState.expanded)
    val workbench = remember(reviewId) {
        val state = session.open(reviewId, defaultName)
        Workbench(
            preferences = preferences,
            onQuit = { current?.let(::save); exit() },
            data = session.data,
            state = state,
            conversation = session.conversation,
            reviews = session.reviews(),
            onOpenReview = { id ->
                current?.let(::save)
                reviewId = id ?: session.create(defaultName).review.id.value
            },
        ).also { current = it }
    }
    DisposableEffect(workbench) { onDispose { workbench.scope.cancel() } }
    StretchWindow(
        onCloseRequest = { save(workbench); onExit() },
        title = stringResource(Res.string.app_name),
        state = rememberWindowState(width = AppConstants.WINDOW_WIDTH, height = AppConstants.WINDOW_HEIGHT),
    ) {
        SavePreferences(workbench) { preferences = it; onPreferencesChange(it) }
        SaveReview(workbench, ::save)
        CompositionLocalProvider(LocalActionText provides actionText()) {
            MacMenuBar(appMenus(), workbench.registry, workbench.keymap, null, onSettings = { workbench.settingsOpen = true }, onQuit = { save(workbench); onExit() })
        }
        StretchTheme(workbench.preferences.theme, scale = workbench.preferences.interfaceSize) {
            ArcFrame(workbench)
        }
    }
}

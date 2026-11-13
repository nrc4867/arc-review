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
import dev.chieppa.arc.ui.actions.actionText
import dev.chieppa.arc.ui.frame.ArcFrame
import dev.chieppa.arc.ui.menus.appMenus
import dev.chieppa.arc.ui.pulls.PullWatch
import dev.chieppa.arc.ui.runs.RunWatch
import dev.chieppa.arc.ui.services.DesktopNotifier
import dev.chieppa.arc.ui.settings.UiPreferences
import dev.chieppa.arc.ui.update.AppUpdates
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.frame.LocalActionText
import dev.chieppa.stretch.frame.MacMenuBar
import dev.chieppa.stretch.frame.StretchWindow
import dev.chieppa.stretch.theme.StretchTheme
import kotlinx.coroutines.cancel
import org.jetbrains.compose.resources.stringResource

/**
 * Arc's main window on [session]'s saved state, opening with the [initial] look on the workspace. Each change to the
 * look goes to [onPreferencesChange], and the workspace is saved as it changes and when the window closes. The saved
 * filters and rules in [watch], and the runs in [runWatch], are polled while the window is open, with notifications
 * through [notifier], and saved as they change. [updates] updates the installed Arc, asked for a newer release when the
 * window opens and daily. [onExit] ends the app.
 */
@Composable
fun ArcWindow(
    session: AppSession,
    initial: UiPreferences,
    watch: PullWatch,
    runWatch: RunWatch,
    notifier: DesktopNotifier,
    onPreferencesChange: suspend (UiPreferences) -> Unit,
    onExit: () -> Unit,
    updates: AppUpdates? = null,
) {
    val exit by rememberUpdatedState(onExit)
    var preferences by remember { mutableStateOf(initial) }
    fun save(workbench: Workbench) {
        session.save(workbench.state, workbench.treeState.expanded)
        session.saveConversation(workbench.comments.controller.local)
        session.saveRecent(workbench.start.recent)
    }
    val workbench = remember {
        lateinit var opened: Workbench
        Workbench(
            preferences = preferences,
            onQuit = { exitStep { save(opened) }; exit() },
            data = session.data,
            state = session.open(),
            conversation = session.conversation,
            watch = watch,
            conversationService = session.conversations,
            localConversation = session.localConversation(),
            codeNavigation = session.codeNavigation,
            startLayout = session.settings.startLayout,
            recent = session.recent(),
            runWatch = runWatch,
            actions = session.actions,
            runKeeper = session.runKeeper,
            desktop = notifier,
            plugins = session::plugins,
            mcp = McpSetupFactory.of(session),
            updates = updates,
        ).also { opened = it }
    }
    DisposableEffect(workbench) {
        onDispose {
            workbench.mcp.stop()
            workbench.scope.cancel()
        }
    }
    StretchWindow(
        onCloseRequest = { exitStep { save(workbench) }; onExit() },
        title = stringResource(Res.string.app_name),
        state = rememberWindowState(width = AppConstants.WINDOW_WIDTH, height = AppConstants.WINDOW_HEIGHT),
    ) {
        SavePreferences(workbench) { preferences = it; onPreferencesChange(it) }
        SaveWorkspace(workbench, ::save)
        SaveWatchlist(watch, workbench.dispatchers, session::saveWatchlist)
        SaveConversation(workbench, session::saveConversation)
        SaveStart(workbench, session::saveStartLayout, session::saveRecent)
        WatchPolling(watch, session.github, notifier, workbench.dispatchers)
        SaveRuns(runWatch, workbench.dispatchers, session::saveRunWatchlist, session::saveTimings)
        RunPolling(workbench)
        UpdatePolling(workbench)
        CompositionLocalProvider(LocalActionText provides actionText()) {
            MacMenuBar(appMenus(), workbench.registry, workbench.keymap, null, onSettings = workbench.panes::openSettings, onQuit = { exitStep { save(workbench) }; onExit() })
        }
        StretchTheme(workbench.preferences.theme, scale = workbench.preferences.interfaceSize) {
            ArcFrame(workbench)
        }
    }
}

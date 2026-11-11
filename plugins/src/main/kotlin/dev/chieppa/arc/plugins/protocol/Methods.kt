package dev.chieppa.arc.plugins.protocol

/**
 * The methods of plugin API version 1, by who calls them. A later version only adds methods and optional fields, so a
 * plugin written for version 1 keeps working.
 */
object Methods {
    // Arc to the plugin.

    /** [InitializeParams] → [InitializeResult]: load the plugin's bundle and say what it contributes. */
    const val INITIALIZE = "initialize"

    /** [PingParams] → nothing: answered at once while the process is alive and not stuck. */
    const val PING = "ping"

    /** [ActionRunParams] → nothing, once the action's `run` has finished. */
    const val ACTION_RUN = "action.run"

    /** [MountParams] → nothing: render a panel, pane or settings page as the view named, sending its tree as patches. */
    const val VIEW_MOUNT = "view.mount"

    /** Notification, [UnmountParams]: the view left the window. */
    const val VIEW_UNMOUNT = "view.unmount"

    /** Notification, [ViewEventParams]: a person used a component; call its handler. */
    const val VIEW_EVENT = "view.event"

    /** Notification, [SettingsChangedParams]: the plugin's settings changed. */
    const val SETTINGS_CHANGED = "settings.changed"

    /** Notification, [AppEventParams]: something happened in Arc, such as a source opening. */
    const val EVENT = "event"

    /** Notification, [ConsoleOutputParams]: a process the plugin started printed something. */
    const val CONSOLE_OUTPUT = "console.output"

    /** Notification, [ConsoleExitParams]: a process the plugin started ended. */
    const val CONSOLE_EXIT = "console.exit"

    // The plugin to Arc.

    /** Notification, [PatchParams]: changes to a mounted view's component tree. */
    const val VIEW_PATCH = "view.patch"

    /** [SettingsSetParams] → nothing. */
    const val SETTINGS_SET = "settings.set"

    /** [StoreGetParams] → the value, or null. */
    const val STORE_GET = "store.get"

    /** [StoreSetParams] → nothing. */
    const val STORE_SET = "store.set"

    /** [StoreGetParams] → nothing: forget the key. */
    const val STORE_DELETE = "store.delete"

    /** [ConsoleRunParams] → [ConsoleRunResult]: start a process Arc runs and shows. */
    const val CONSOLE_RUN = "console.run"

    /** [ConsoleWriteParams] → nothing: type into a process's input. */
    const val CONSOLE_WRITE = "console.write"

    /** [ConsoleHandleParams] → nothing: end a process. */
    const val CONSOLE_KILL = "console.kill"

    /** [PanesOpenParams] → nothing: open one of the plugin's panes in the grid. */
    const val PANES_OPEN = "panes.open"

    /** [GitGrepParams] → [GitGrepResult]: search a source's files. */
    const val GIT_GREP = "git.grep"
}

package dev.chieppa.arc.plugins.protocol

/**
 * The methods of plugin API version 1, by who calls them. A later version only adds methods and optional fields, so a
 * plugin written for version 1 keeps working.
 */
object Methods {
    // Arc to the Node process itself, with no plugin named.

    /** [StartWorkerParams] → nothing, once the plugin's worker thread is started. */
    const val HOST_START_WORKER = "host.startWorker"

    /** [StopWorkerParams] → nothing, once the plugin's worker thread is ended. */
    const val HOST_STOP_WORKER = "host.stopWorker"

    /** Notification from the process, [WorkerExitParams]: a plugin's worker ended. */
    const val HOST_WORKER_EXIT = "host.workerExit"

    // Arc to the plugin; `ping` also goes to the process itself.

    /** [InitializeParams] → [InitializeResult]: load the plugin's entry and say what it contributes. */
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

    /** Notification, [LogParams]: something the plugin printed. */
    const val LOG = "log"

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

    /** [FilesReadParams] → [FilesReadResult]: read a file in a folder the manifest names. */
    const val FILES_READ = "files.read"

    /** [FilesWriteParams] → nothing: write a file in a folder the manifest names. */
    const val FILES_WRITE = "files.write"

    /** [FetchParams] → [FetchResult]: an HTTP request Arc makes to a host the manifest names. */
    const val NET_FETCH = "net.fetch"

    /** [NotesListParams] → [NotesListResult]: the person's notes. */
    const val NOTES_LIST = "notes.list"

    /** [NoteCreateParams] → [NoteInfo]: add a note. */
    const val NOTES_CREATE = "notes.create"

    /** [NoteCreateParams] → [NoteInfo]: start a comment draft, which only the person stages and pushes. */
    const val COMMENTS_DRAFT = "comments.draft"

    /** [OutputAppendParams] → nothing: write to the plugin's own channel in the Console. */
    const val OUTPUT_APPEND = "output.append"

    /** No params → nothing: empty the plugin's own channel in the Console. */
    const val OUTPUT_CLEAR = "output.clear"
}

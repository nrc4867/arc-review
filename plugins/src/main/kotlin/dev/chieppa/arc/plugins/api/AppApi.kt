package dev.chieppa.arc.plugins.api

import dev.chieppa.arc.plugins.protocol.AppEvents

/**
 * The `app` object every plugin function gets, as interfaces, the source of the generated types. Each member is a
 * protocol method or a part of the call's context, and Arc checks each call against the plugin's capabilities.
 */
object AppApi {
    private fun member(name: String, ts: String, doc: String, optional: Boolean = false) = PropSpec(name, PropType.Named(ts), doc, optional)

    private fun method(name: String, signature: String, doc: String) = PropSpec(name, PropType.Named(signature), doc, method = true)

    /** The events a plugin hears, through definePlugin's `on` and `app.on`. */
    val events: List<FieldDoc> = listOf(
        FieldDoc(AppEvents.SOURCE_OPENED, "AppEvent", "A source opened in the strip; event.source names it."),
        FieldDoc(AppEvents.FILE_MARKED, "AppEvent", "A file was marked or unmarked; event.file names it, event.data.kind the mark or null."),
        FieldDoc(AppEvents.PUSH_FINISHED, "AppEvent", "A push of a review to its host ended; event.source names the source."),
        FieldDoc(AppEvents.NOTES_CHANGED, "AppEvent", "The person's notes on a source changed; event.data.ids names them. Needs notes.read."),
    )

    val eventNames: List<String> = events.map { it.name }

    val interfaces: List<InterfaceSpec> = listOf(
        InterfaceSpec(
            "App<S = Record<string, unknown>>",
            "What a plugin can read and do. Every call carries the source it acts on.",
            listOf(
                member("plugin", "{ readonly id: string; readonly version: string }", "This plugin."),
                member("activeSource", "SourceInfo | null", "The source this call acts on: the one in focus, or the one the view was opened for."),
                member("focus", "{ readonly file: FileInfo | null }", "The file in focus in it."),
                member("props", "Readonly<Record<string, unknown>>", "What a pane was opened with."),
                member("settings", "Readonly<S>", "The plugin's settings, as stored or their defaults."),
                method("setSetting", "<K extends keyof S & string>(key: K, value: S[K]): Promise<void>", "Stores a setting; open views redraw."),
                member("onSettingsChange", "(listener: (settings: Readonly<S>) => void) => () => void", "Listens for setting changes; gives a function that stops."),
                member("store", "PluginStore", "The plugin's private store."),
                member("console", "ConsoleApi", "Processes Arc runs for the plugin. Needs the console capability."),
                member("panes", "PanesApi", "The plugin's panes in the grid. Needs the panes capability."),
                member("git", "GitApi", "Queries on the sources' repos. Needs the git capability."),
                member("notes", "NotesApi", "The person's notes. Needs the notes capability: read to list them, write to add one."),
                member("comments", "CommentsApi", "Comment drafts, which only the person stages and pushes. Needs the comments capability's draft."),
                member("files", "FilesApi", "Files in the folders the manifest's files capability names."),
                member("fetch", "(url: string, init?: FetchInit) => Promise<FetchResponse>", "An HTTP request Arc makes to a host the manifest's network capability names, with no credentials of Arc's and no redirects followed."),
                member("on", "(event: AppEventName, listener: (event: AppEvent) => void) => () => void", "Listens for an event in Arc; gives a function that stops."),
                member("useQuery", "<T>(load: () => Promise<T>, deps?: readonly unknown[]) => T | undefined", "A hook: what load gives, undefined until it has, loaded again when deps or the source change."),
                member("log", "(...values: unknown[]) => void", "Writes to the plugin's log."),
            ),
        ),
        InterfaceSpec(
            "PluginStore",
            "Values the plugin keeps between runs, by key, as JSON.",
            listOf(
                member("get", "<T = unknown>(key: string) => Promise<T | undefined>", "The value under key."),
                member("set", "(key: string, value: unknown) => Promise<void>", "Keeps value under key."),
                member("delete", "(key: string) => Promise<void>", "Forgets key."),
            ),
        ),
        InterfaceSpec(
            "ConsoleApi",
            "Processes Arc runs for the plugin, shown by Console components.",
            listOf(
                member("run", "(command: readonly string[], options?: { in?: \"worktree\" | \"plugin\" | string }) => Promise<ProcessHandle>", "Starts a program with no shell between, in the active source's worktree by default."),
                member("attach", "(id: string) => ProcessHandle", "A handle on a process started earlier, by its id."),
            ),
        ),
        InterfaceSpec(
            "ProcessHandle",
            "A process Arc runs for the plugin.",
            listOf(
                member("id", "string", "Its id, which a Console's process prop or a pane's props can carry."),
                member("write", "(text: string) => Promise<void>", "Types text into its input."),
                member("kill", "() => Promise<void>", "Ends it."),
                member("onOutput", "(listener: (text: string, error: boolean) => void) => () => void", "Listens for what it prints."),
                member("onExit", "(listener: (code: number) => void) => () => void", "Listens for its end."),
            ),
        ),
        InterfaceSpec(
            "PanesApi",
            "The plugin's own panes.",
            listOf(member("open", "(pane: string, props?: Record<string, unknown>) => Promise<void>", "Opens one of the plugin's panes, with props as JSON.")),
        ),
        InterfaceSpec(
            "GitApi",
            "Queries on the repos of the open sources.",
            listOf(member("grep", "(pattern: string, options?: { limit?: number }) => Promise<GrepHit[]>", "Lines matching a regular expression in the active source's files.")),
        ),
        InterfaceSpec(
            "NotesApi",
            "The person's notes, kept on this machine.",
            listOf(
                member("list", "(filter?: NotesFilter) => Promise<NoteInfo[]>", "The notes, oldest first, only those on a source or file when the filter names one. Needs notes.read."),
                member("create", "(note: NewNote) => Promise<NoteInfo>", "Adds a note, on the active source unless it names another. Needs notes.write."),
            ),
        ),
        InterfaceSpec(
            "CommentsApi",
            "Comments the person may push to the host.",
            listOf(member("draft", "(comment: NewNote) => Promise<NoteInfo>", "Starts a comment draft, which stays on this machine until the person stages and pushes it. Needs comments.draft.")),
        ),
        InterfaceSpec(
            "NotesFilter",
            "Which notes app.notes.list gives.",
            listOf(
                member("source", "string", "Only those on the source with this ref.", optional = true),
                member("path", "string", "Only those on this file.", optional = true),
            ),
        ),
        InterfaceSpec(
            "NewNote",
            "A note or comment draft to add: on lines of a file, or on the source as a whole.",
            listOf(
                member("text", "string", "Its text, as Markdown."),
                member("source", "string", "The ref of the source it is on; the active source when absent.", optional = true),
                member("path", "string", "The file it is on.", optional = true),
                member("line", "number", "The line it is on, from 1; needs path.", optional = true),
                member("startLine", "number", "The first line of a range ending at line.", optional = true),
                member("side", "NoteSide", "The side of the change its lines are on; new when absent.", optional = true),
            ),
        ),
        InterfaceSpec(
            "FilesApi",
            "Files a plugin reads and writes through Arc. Paths are absolute or relative to the plugin's folder.",
            listOf(
                member("read", "(path: string) => Promise<string>", "A file's text, under a folder the manifest lets it read."),
                member("write", "(path: string, text: string) => Promise<void>", "Writes a file's text, under a folder the manifest lets it write."),
            ),
        ),
        InterfaceSpec(
            "FetchInit",
            "How app.fetch sends a request.",
            listOf(
                member("method", "string", "GET when absent.", optional = true),
                member("headers", "Readonly<Record<string, string>>", "Request headers.", optional = true),
                member("body", "string", "The request's body as text.", optional = true),
            ),
        ),
        InterfaceSpec(
            "FetchResponse",
            "What app.fetch answers with.",
            listOf(
                member("status", "number", "The HTTP status."),
                member("ok", "boolean", "Whether the status is 200 to 299."),
                member("headers", "Readonly<Record<string, string>>", "The first value of each header, by lowercase name."),
                member("text", "() => Promise<string>", "The body as text."),
                member("json", "<T = unknown>() => Promise<T>", "The body read as JSON."),
            ),
        ),
        InterfaceSpec(
            "AppEvent",
            "Something that happened in Arc.",
            listOf(
                member("name", "AppEventName", "What happened."),
                member("source", "SourceInfo | null", "The source it happened in."),
                member("file", "FileInfo | null", "The file it happened to."),
                member("data", "Readonly<Record<string, unknown>>", "What else it carries."),
            ),
        ),
    )
}

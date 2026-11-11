package dev.chieppa.arc.plugins.api

import dev.chieppa.arc.plugins.protocol.AppEvents

/**
 * The `app` object every plugin function gets, as interfaces, the source of the generated types. Each member is a
 * protocol method or a part of the call's context, and Arc checks each call against the plugin's capabilities.
 */
object AppApi {
    private fun member(name: String, ts: String, doc: String, optional: Boolean = false) = PropSpec(name, PropType.Named(ts), doc, optional)

    val eventNames: List<String> = listOf(AppEvents.SOURCE_OPENED, AppEvents.FILE_MARKED, AppEvents.PUSH_FINISHED)

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
                member("setSetting", "<K extends keyof S & string>(key: K, value: S[K]) => Promise<void>", "Stores a setting; open views redraw."),
                member("onSettingsChange", "(listener: (settings: Readonly<S>) => void) => () => void", "Listens for setting changes; gives a function that stops."),
                member("store", "PluginStore", "The plugin's private store."),
                member("console", "ConsoleApi", "Processes Arc runs for the plugin. Needs the console capability."),
                member("panes", "PanesApi", "The plugin's panes in the grid. Needs the panes capability."),
                member("git", "GitApi", "Queries on the sources' repos. Needs the git capability."),
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

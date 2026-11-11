// Generated from Arc's Kotlin definitions by ./gradlew :plugins:generatePluginTypes. Do not edit.

import type { ReactNode } from "react";

export type ApiVersion = 1;

export type IconName = "branch" | "check" | "close" | "code" | "comment" | "commit" | "console" | "file" | "filter" | "folder" | "info" | "link" | "note" | "play" | "plugin" | "plus" | "pull-request" | "refresh" | "runs" | "search" | "settings" | "spec" | "stop" | "tag" | "warning";

export type AppEventName = "source.opened" | "file.marked" | "push.finished" | "notes.changed";

export type SettingType = "text" | "number" | "boolean" | "choice" | "glob" | "list" | "map" | "secret";

export type NoteKind = "note" | "draft";

export type NoteSide = "old" | "new";

export interface RepoInfo {
    readonly id: string;
    readonly name: string;
    readonly githubId?: number;
}

export interface SourceInfo {
    readonly ref: string;
    readonly kind: string;
    readonly repo: RepoInfo;
    readonly worktree?: string;
    readonly head?: string;
}

export interface FileInfo {
    readonly path: string;
    readonly line?: number;
}

export interface GrepHit {
    readonly path: string;
    readonly line: number;
    readonly text: string;
}

export interface NoteInfo {
    readonly id: string;
    readonly kind: NoteKind;
    readonly source: string;
    readonly repo: RepoInfo;
    readonly text: string;
    readonly createdAt: number;
    readonly updatedAt: number;
    readonly path?: string;
    readonly line?: number;
    readonly startLine?: number;
    readonly side?: NoteSide;
    readonly revision?: string;
}

/** A setting a plugin declares in definePlugin. */
export interface SettingSpec {
    readonly type: SettingType;
    readonly default?: unknown;
    readonly label?: string;
    readonly of?: SettingType;
    readonly options?: readonly string[];
}

type ScalarValue<T> = T extends "number" ? number : T extends "boolean" ? boolean : string;

/** The value a setting of spec T holds. */
export type SettingValue<T extends SettingSpec> = T["type"] extends "list"
    ? ScalarValue<T["of"]>[]
    : T["type"] extends "map"
      ? Record<string, ScalarValue<T["of"]>>
      : ScalarValue<T["type"]>;

/** The values of the settings D declares. */
export type SettingsOf<D> = { [K in keyof D]: D[K] extends SettingSpec ? SettingValue<D[K]> : never };

/** Children one under another. */
export interface ColumnProps {
    /** What it holds. */
    readonly children?: ReactNode;
    /** Space between children, in dp. */
    readonly gap?: number;
    /** Space around the children, in dp. */
    readonly padding?: number;
    /** Take the room left in its parent. */
    readonly grow?: boolean;
}

/** Children side by side. */
export interface RowProps {
    /** What it holds. */
    readonly children?: ReactNode;
    /** Space between children, in dp. */
    readonly gap?: number;
    /** Space around the children, in dp. */
    readonly padding?: number;
    /** Take the room left in its parent. */
    readonly grow?: boolean;
    /** Where children sit across the row. */
    readonly align?: "start" | "center" | "end";
}

/** Two children sharing the room, side by side or stacked. */
export interface SplitProps {
    /** What it holds. */
    readonly children?: ReactNode;
    /** Side by side, or stacked. */
    readonly direction?: "horizontal" | "vertical";
    /** The first child's share, from 0 to 1. */
    readonly ratio?: number;
    /** Take the room left in its parent. */
    readonly grow?: boolean;
}

/** Children that scroll when they do not fit. */
export interface ScrollProps {
    /** What it holds. */
    readonly children?: ReactNode;
    /** Space around the children, in dp. */
    readonly padding?: number;
    /** Take the room left in its parent. */
    readonly grow?: boolean;
}

/** Rows for items, built only while on screen. */
export interface ListProps<T> {
    /** The items; none while undefined. */
    readonly items: readonly T[] | undefined;
    /** A row for each item, usually a ListRow. */
    readonly children: (item: T, index: number) => ReactNode;
    /** A row was clicked. */
    readonly onSelect?: (item: T) => void;
    /** The selected row's index. */
    readonly selected?: number;
    /** Take the room left in its parent. */
    readonly grow?: boolean;
}

/** One row of a List. */
export interface ListRowProps {
    /** Its text. */
    readonly title: string;
    /** Quieter text after it. */
    readonly detail?: string;
    /** An icon by name. */
    readonly icon?: IconName;
    /** Take the room left in its parent. */
    readonly grow?: boolean;
}

/** Rows of cells under column headings. */
export interface TableProps {
    /** The headings. */
    readonly columns: readonly string[];
    /** Each row's cells, in column order. */
    readonly rows: readonly readonly string[][];
    /** A row was clicked, by its index. */
    readonly onSelect?: (row: number) => void;
    /** Take the room left in its parent. */
    readonly grow?: boolean;
}

/** A button. */
export interface ButtonProps {
    /** Its text. */
    readonly label: string;
    /** It was pressed. */
    readonly onClick?: () => void;
    /** How strongly it is drawn. */
    readonly kind?: "default" | "primary" | "quiet";
    /** Shown faint, taking no clicks. */
    readonly disabled?: boolean;
    /** An icon by name. */
    readonly icon?: IconName;
    /** Take the room left in its parent. */
    readonly grow?: boolean;
}

/** An on/off switch. */
export interface ToggleProps {
    /** Whether it is on. */
    readonly checked: boolean;
    /** It was flipped. */
    readonly onChange: (checked: boolean) => void;
    /** What it switches, for assistive tools. */
    readonly label: string;
    /** Take the room left in its parent. */
    readonly grow?: boolean;
}

/** A one-line text field. */
export interface FieldProps {
    /** Its text. */
    readonly value: string;
    /** The text was edited. */
    readonly onChange: (value: string) => void;
    /** Enter was pressed. */
    readonly onSubmit?: (value: string) => void;
    /** Shown while empty. */
    readonly placeholder?: string;
    /** Set in the code face, for paths and patterns. */
    readonly mono?: boolean;
    /** Shows a dot for each character, for a setting of type secret. */
    readonly secret?: boolean;
    /** Take the room left in its parent. */
    readonly grow?: boolean;
}

/** A menu of choices. */
export interface SelectProps {
    /** The choices. */
    readonly options: readonly string[];
    /** The chosen one. */
    readonly value: string;
    /** Another was chosen. */
    readonly onChange: (value: string) => void;
    /** What it chooses, for assistive tools. */
    readonly label: string;
    /** Take the room left in its parent. */
    readonly grow?: boolean;
}

/** A run of text. */
export interface TextProps {
    /** The text. */
    readonly children: string | number | readonly (string | number)[];
    /** Its ink. */
    readonly tone?: "default" | "muted" | "strong" | "error";
    /** Set in the code face. */
    readonly mono?: boolean;
    /** Take the room left in its parent. */
    readonly grow?: boolean;
}

/** Markdown, rendered. */
export interface MarkdownProps {
    /** The Markdown. */
    readonly text: string;
    /** Take the room left in its parent. */
    readonly grow?: boolean;
}

/** Code with line numbers and colours. */
export interface CodeProps {
    /** The code. */
    readonly text: string;
    /** Its language, such as typescript. */
    readonly language?: string;
    /** Take the room left in its parent. */
    readonly grow?: boolean;
}

/** A process's output, taking typed input, as Arc's console draws it. */
export interface ConsoleProps {
    /** The process app.console.run started, or its id. */
    readonly process: ProcessHandle | string;
    /** Its height in dp; it fills its parent without one. */
    readonly height?: number;
    /** Take the room left in its parent. */
    readonly grow?: boolean;
}

/** What a plugin can read and do. Every call carries the source it acts on. */
export interface App<S = Record<string, unknown>> {
    /** This plugin. */
    readonly plugin: { readonly id: string; readonly version: string };
    /** The source this call acts on: the one in focus, or the one the view was opened for. */
    readonly activeSource: SourceInfo | null;
    /** The file in focus in it. */
    readonly focus: { readonly file: FileInfo | null };
    /** What a pane was opened with. */
    readonly props: Readonly<Record<string, unknown>>;
    /** The plugin's settings, as stored or their defaults. */
    readonly settings: Readonly<S>;
    /** Stores a setting; open views redraw. */
    setSetting<K extends keyof S & string>(key: K, value: S[K]): Promise<void>;
    /** Listens for setting changes; gives a function that stops. */
    readonly onSettingsChange: (listener: (settings: Readonly<S>) => void) => () => void;
    /** The plugin's private store. */
    readonly store: PluginStore;
    /** Processes Arc runs for the plugin. Needs the console capability. */
    readonly console: ConsoleApi;
    /** The plugin's panes in the grid. Needs the panes capability. */
    readonly panes: PanesApi;
    /** Queries on the sources' repos. Needs the git capability. */
    readonly git: GitApi;
    /** The person's notes. Needs the notes capability: read to list them, write to add one. */
    readonly notes: NotesApi;
    /** Comment drafts, which only the person stages and pushes. Needs the comments capability's draft. */
    readonly comments: CommentsApi;
    /** Files in the folders the manifest's files capability names. */
    readonly files: FilesApi;
    /** An HTTP request Arc makes to a host the manifest's network capability names, with no credentials of Arc's and no redirects followed. */
    readonly fetch: (url: string, init?: FetchInit) => Promise<FetchResponse>;
    /** Listens for an event in Arc; gives a function that stops. */
    readonly on: (event: AppEventName, listener: (event: AppEvent) => void) => () => void;
    /** A hook: what load gives, undefined until it has, loaded again when deps or the source change. */
    readonly useQuery: <T>(load: () => Promise<T>, deps?: readonly unknown[]) => T | undefined;
    /** Writes to the plugin's log. */
    readonly log: (...values: unknown[]) => void;
}

/** Values the plugin keeps between runs, by key, as JSON. */
export interface PluginStore {
    /** The value under key. */
    readonly get: <T = unknown>(key: string) => Promise<T | undefined>;
    /** Keeps value under key. */
    readonly set: (key: string, value: unknown) => Promise<void>;
    /** Forgets key. */
    readonly delete: (key: string) => Promise<void>;
}

/** Processes Arc runs for the plugin, shown by Console components. */
export interface ConsoleApi {
    /** Starts a program with no shell between, in the active source's worktree by default. */
    readonly run: (command: readonly string[], options?: { in?: "worktree" | "plugin" | string }) => Promise<ProcessHandle>;
    /** A handle on a process started earlier, by its id. */
    readonly attach: (id: string) => ProcessHandle;
}

/** A process Arc runs for the plugin. */
export interface ProcessHandle {
    /** Its id, which a Console's process prop or a pane's props can carry. */
    readonly id: string;
    /** Types text into its input. */
    readonly write: (text: string) => Promise<void>;
    /** Ends it. */
    readonly kill: () => Promise<void>;
    /** Listens for what it prints. */
    readonly onOutput: (listener: (text: string, error: boolean) => void) => () => void;
    /** Listens for its end. */
    readonly onExit: (listener: (code: number) => void) => () => void;
}

/** The plugin's own panes. */
export interface PanesApi {
    /** Opens one of the plugin's panes, with props as JSON. */
    readonly open: (pane: string, props?: Record<string, unknown>) => Promise<void>;
}

/** Queries on the repos of the open sources. */
export interface GitApi {
    /** Lines matching a regular expression in the active source's files. */
    readonly grep: (pattern: string, options?: { limit?: number }) => Promise<GrepHit[]>;
}

/** The person's notes, kept on this machine. */
export interface NotesApi {
    /** The notes, oldest first, only those on a source or file when the filter names one. Needs notes.read. */
    readonly list: (filter?: NotesFilter) => Promise<NoteInfo[]>;
    /** Adds a note, on the active source unless it names another. Needs notes.write. */
    readonly create: (note: NewNote) => Promise<NoteInfo>;
}

/** Comments the person may push to the host. */
export interface CommentsApi {
    /** Starts a comment draft, which stays on this machine until the person stages and pushes it. Needs comments.draft. */
    readonly draft: (comment: NewNote) => Promise<NoteInfo>;
}

/** Which notes app.notes.list gives. */
export interface NotesFilter {
    /** Only those on the source with this ref. */
    readonly source?: string;
    /** Only those on this file. */
    readonly path?: string;
}

/** A note or comment draft to add: on lines of a file, or on the source as a whole. */
export interface NewNote {
    /** Its text, as Markdown. */
    readonly text: string;
    /** The ref of the source it is on; the active source when absent. */
    readonly source?: string;
    /** The file it is on. */
    readonly path?: string;
    /** The line it is on, from 1; needs path. */
    readonly line?: number;
    /** The first line of a range ending at line. */
    readonly startLine?: number;
    /** The side of the change its lines are on; new when absent. */
    readonly side?: NoteSide;
}

/** Files a plugin reads and writes through Arc. Paths are absolute or relative to the plugin's folder. */
export interface FilesApi {
    /** A file's text, under a folder the manifest lets it read. */
    readonly read: (path: string) => Promise<string>;
    /** Writes a file's text, under a folder the manifest lets it write. */
    readonly write: (path: string, text: string) => Promise<void>;
}

/** How app.fetch sends a request. */
export interface FetchInit {
    /** GET when absent. */
    readonly method?: string;
    /** Request headers. */
    readonly headers?: Readonly<Record<string, string>>;
    /** The request's body as text. */
    readonly body?: string;
}

/** What app.fetch answers with. */
export interface FetchResponse {
    /** The HTTP status. */
    readonly status: number;
    /** Whether the status is 200 to 299. */
    readonly ok: boolean;
    /** The first value of each header, by lowercase name. */
    readonly headers: Readonly<Record<string, string>>;
    /** The body as text. */
    readonly text: () => Promise<string>;
    /** The body read as JSON. */
    readonly json: <T = unknown>() => Promise<T>;
}

/** Something that happened in Arc. */
export interface AppEvent {
    /** What happened. */
    readonly name: AppEventName;
    /** The source it happened in. */
    readonly source: SourceInfo | null;
    /** The file it happened to. */
    readonly file: FileInfo | null;
    /** What else it carries. */
    readonly data: Readonly<Record<string, unknown>>;
}

# Arc plugin API reference

Plugin API version 1. Generated from Arc's Kotlin definitions by ./gradlew :plugins:generatePluginTypes; the types in `arc.d.ts` come from the same definitions. Do not edit.

## plugin.json

| Key | Type | What |
| --- | --- | --- |
| `id` | `string` | The plugin's id: lowercase letters, digits, dots, dashes and underscores, at most 64. |
| `version` | `string` | Its version, shown in Settings › Plugins. |
| `apiVersion` | `number` | The plugin API version it is written for. |
| `entry` | `string` | The TypeScript or JavaScript file whose default export is definePlugin(...), relative to its folder. |
| `name` | `string` | The name shown; the id when absent. |
| `description` | `string` | One line on what it does. |
| `icon` | `IconName` | Its icon in Settings › Plugins. |
| `capabilities` | `object` | What it may do, shown to the person before any of its code runs; see Capabilities. |

### Capabilities

A plugin declares in `capabilities` everything it may do. Arc checks every call against them, drops contributions of kinds it does not declare, and a later version that asks for more stays off until the person enables it again.

| Key | Type | What |
| --- | --- | --- |
| `panels` | `boolean` | Adds drawers with their own rail icons: definePlugin's panels. |
| `panes` | `boolean` | Adds pane kinds to the grid, definePlugin's panes, and opens them with app.panes.open. |
| `actions` | `boolean` | Adds commands to the palette, menus and keys: definePlugin's actions. |
| `console` | `boolean` | Runs programs through app.console.run; Arc runs them and a Console shows them. |
| `git` | `boolean` | Queries the open sources' repos through app.git. |
| `notes` | `{ read?: boolean; write?: boolean }` | app.notes: read lists the person's notes and hears notes.changed; write adds notes. |
| `comments` | `{ draft?: boolean }` | app.comments.draft starts a comment draft, which only the person stages and pushes. |
| `files` | `{ read?: string[]; write?: string[] }` | The folders app.files may read and write, relative to the plugin's folder or absolute. |
| `network` | `string[]` | The hosts app.fetch may reach; *.host names its subdomains. |
| `mcp` | `string[]` | The MCP server roles the plugin calls. |

## definePlugin

The entry file's default export is `definePlugin({ ... })` from `@review/plugin`, with any of:

| Key | What |
| --- | --- |
| `settings` | The settings it keeps, by key: `{ type, default, label, of, options }`; `app.settings` holds their values. |
| `settingsPage` | `(app) => ReactNode`: its own page under Settings › Plugins. |
| `actions` | `{ id, label, keys?, icon?, run(app, context) }[]`: commands in the palette, menus and keys. Needs `actions`. |
| `panels` | `{ id, title, icon?, render(app) }[]`: drawers with their own rail icons. Needs `panels`. |
| `panes` | `{ id, title, icon?, render(app) }[]`: pane kinds in the grid; `app.props` holds what one was opened with. Needs `panes`. |
| `on` | `{ [event]: (app, event) => void }`: what to do when something happens in Arc. |

A component that takes the app object types it as `App` (or `App<Settings>` to read typed settings).

## Components

Import components from `@review/plugin`. Arc draws each with its own Stretch composable, so there is no CSS and no DOM. `List` takes `items` and a function drawing each as a `List.Row`; `Console` takes the handle `app.console.run` gives, or its id.

### Column

Children one under another.

| Prop | Type | What |
| --- | --- | --- |
| `children?` | `ReactNode` | What it holds. |
| `gap?` | `number` | Space between children, in dp. |
| `padding?` | `number` | Space around the children, in dp. |
| `grow?` | `boolean` | Take the room left in its parent. |

### Row

Children side by side.

| Prop | Type | What |
| --- | --- | --- |
| `children?` | `ReactNode` | What it holds. |
| `gap?` | `number` | Space between children, in dp. |
| `padding?` | `number` | Space around the children, in dp. |
| `grow?` | `boolean` | Take the room left in its parent. |
| `align?` | `"start" \| "center" \| "end"` | Where children sit across the row. |

### Split

Two children sharing the room, side by side or stacked.

| Prop | Type | What |
| --- | --- | --- |
| `children?` | `ReactNode` | What it holds. |
| `direction?` | `"horizontal" \| "vertical"` | Side by side, or stacked. |
| `ratio?` | `number` | The first child's share, from 0 to 1. |
| `grow?` | `boolean` | Take the room left in its parent. |

### Scroll

Children that scroll when they do not fit.

| Prop | Type | What |
| --- | --- | --- |
| `children?` | `ReactNode` | What it holds. |
| `padding?` | `number` | Space around the children, in dp. |
| `grow?` | `boolean` | Take the room left in its parent. |

### List

Rows for items, built only while on screen.

| Prop | Type | What |
| --- | --- | --- |
| `items` | `readonly T[] \| undefined` | The items; none while undefined. |
| `children` | `(item: T, index: number) => ReactNode` | A row for each item, usually a ListRow. |
| `onSelect?` | `(item: T) => void` | A row was clicked. |
| `selected?` | `number` | The selected row's index. |
| `grow?` | `boolean` | Take the room left in its parent. |

### ListRow

One row of a List.

| Prop | Type | What |
| --- | --- | --- |
| `title` | `string` | Its text. |
| `detail?` | `string` | Quieter text after it. |
| `icon?` | `IconName` | An icon by name. |
| `grow?` | `boolean` | Take the room left in its parent. |

### Table

Rows of cells under column headings.

| Prop | Type | What |
| --- | --- | --- |
| `columns` | `readonly string[]` | The headings. |
| `rows` | `readonly readonly string[][]` | Each row's cells, in column order. |
| `onSelect?` | `(row: number) => void` | A row was clicked, by its index. |
| `grow?` | `boolean` | Take the room left in its parent. |

### Button

A button.

| Prop | Type | What |
| --- | --- | --- |
| `label` | `string` | Its text. |
| `onClick?` | `() => void` | It was pressed. |
| `kind?` | `"default" \| "primary" \| "quiet"` | How strongly it is drawn. |
| `disabled?` | `boolean` | Shown faint, taking no clicks. |
| `icon?` | `IconName` | An icon by name. |
| `grow?` | `boolean` | Take the room left in its parent. |

### Toggle

An on/off switch.

| Prop | Type | What |
| --- | --- | --- |
| `checked` | `boolean` | Whether it is on. |
| `onChange` | `(checked: boolean) => void` | It was flipped. |
| `label` | `string` | What it switches, for assistive tools. |
| `grow?` | `boolean` | Take the room left in its parent. |

### Field

A one-line text field.

| Prop | Type | What |
| --- | --- | --- |
| `value` | `string` | Its text. |
| `onChange` | `(value: string) => void` | The text was edited. |
| `onSubmit?` | `(value: string) => void` | Enter was pressed. |
| `placeholder?` | `string` | Shown while empty. |
| `mono?` | `boolean` | Set in the code face, for paths and patterns. |
| `secret?` | `boolean` | Shows a dot for each character, for a setting of type secret. |
| `grow?` | `boolean` | Take the room left in its parent. |

### Select

A menu of choices.

| Prop | Type | What |
| --- | --- | --- |
| `options` | `readonly string[]` | The choices. |
| `value` | `string` | The chosen one. |
| `onChange` | `(value: string) => void` | Another was chosen. |
| `label` | `string` | What it chooses, for assistive tools. |
| `grow?` | `boolean` | Take the room left in its parent. |

### Text

A run of text.

| Prop | Type | What |
| --- | --- | --- |
| `children` | `string \| number \| readonly (string \| number)[]` | The text. |
| `tone?` | `"default" \| "muted" \| "strong" \| "error"` | Its ink. |
| `mono?` | `boolean` | Set in the code face. |
| `grow?` | `boolean` | Take the room left in its parent. |

### Markdown

Markdown, rendered.

| Prop | Type | What |
| --- | --- | --- |
| `text` | `string` | The Markdown. |
| `grow?` | `boolean` | Take the room left in its parent. |

### Code

Code with line numbers and colours.

| Prop | Type | What |
| --- | --- | --- |
| `text` | `string` | The code. |
| `language?` | `string` | Its language, such as typescript. |
| `grow?` | `boolean` | Take the room left in its parent. |

### Console

A process's output, taking typed input, as Arc's console draws it.

| Prop | Type | What |
| --- | --- | --- |
| `process` | `ProcessHandle \| string` | The process app.console.run started, or its id. |
| `height?` | `number` | Its height in dp; it fills its parent without one. |
| `grow?` | `boolean` | Take the room left in its parent. |

## The app object

### App

What a plugin can read and do. Every call carries the source it acts on.

| Member | Type | What |
| --- | --- | --- |
| `plugin` | `{ readonly id: string; readonly version: string }` | This plugin. |
| `activeSource` | `SourceInfo \| null` | The source this call acts on: the one in focus, or the one the view was opened for. |
| `focus` | `{ readonly file: FileInfo \| null }` | The file in focus in it. |
| `props` | `Readonly<Record<string, unknown>>` | What a pane was opened with. |
| `settings` | `Readonly<S>` | The plugin's settings, as stored or their defaults. |
| `setSetting` | `<K extends keyof S & string>(key: K, value: S[K]): Promise<void>` | Stores a setting; open views redraw. |
| `onSettingsChange` | `(listener: (settings: Readonly<S>) => void) => () => void` | Listens for setting changes; gives a function that stops. |
| `store` | `PluginStore` | The plugin's private store. |
| `console` | `ConsoleApi` | Processes Arc runs for the plugin. Needs the console capability. |
| `panes` | `PanesApi` | The plugin's panes in the grid. Needs the panes capability. |
| `git` | `GitApi` | Queries on the sources' repos. Needs the git capability. |
| `notes` | `NotesApi` | The person's notes. Needs the notes capability: read to list them, write to add one. |
| `comments` | `CommentsApi` | Comment drafts, which only the person stages and pushes. Needs the comments capability's draft. |
| `files` | `FilesApi` | Files in the folders the manifest's files capability names. |
| `fetch` | `(url: string, init?: FetchInit) => Promise<FetchResponse>` | An HTTP request Arc makes to a host the manifest's network capability names, with no credentials of Arc's and no redirects followed. |
| `on` | `(event: AppEventName, listener: (event: AppEvent) => void) => () => void` | Listens for an event in Arc; gives a function that stops. |
| `useQuery` | `<T>(load: () => Promise<T>, deps?: readonly unknown[]) => T \| undefined` | A hook: what load gives, undefined until it has, loaded again when deps or the source change. |
| `log` | `(...values: unknown[]) => void` | Writes to the plugin's log. |

### PluginStore

Values the plugin keeps between runs, by key, as JSON.

| Member | Type | What |
| --- | --- | --- |
| `get` | `<T = unknown>(key: string) => Promise<T \| undefined>` | The value under key. |
| `set` | `(key: string, value: unknown) => Promise<void>` | Keeps value under key. |
| `delete` | `(key: string) => Promise<void>` | Forgets key. |

### ConsoleApi

Processes Arc runs for the plugin, shown by Console components.

| Member | Type | What |
| --- | --- | --- |
| `run` | `(command: readonly string[], options?: { in?: "worktree" \| "plugin" \| string }) => Promise<ProcessHandle>` | Starts a program with no shell between, in the active source's worktree by default. |
| `attach` | `(id: string) => ProcessHandle` | A handle on a process started earlier, by its id. |

### ProcessHandle

A process Arc runs for the plugin.

| Member | Type | What |
| --- | --- | --- |
| `id` | `string` | Its id, which a Console's process prop or a pane's props can carry. |
| `write` | `(text: string) => Promise<void>` | Types text into its input. |
| `kill` | `() => Promise<void>` | Ends it. |
| `onOutput` | `(listener: (text: string, error: boolean) => void) => () => void` | Listens for what it prints. |
| `onExit` | `(listener: (code: number) => void) => () => void` | Listens for its end. |

### PanesApi

The plugin's own panes.

| Member | Type | What |
| --- | --- | --- |
| `open` | `(pane: string, props?: Record<string, unknown>) => Promise<void>` | Opens one of the plugin's panes, with props as JSON. |

### GitApi

Queries on the repos of the open sources.

| Member | Type | What |
| --- | --- | --- |
| `grep` | `(pattern: string, options?: { limit?: number }) => Promise<GrepHit[]>` | Lines matching a regular expression in the active source's files. |

### NotesApi

The person's notes, kept on this machine.

| Member | Type | What |
| --- | --- | --- |
| `list` | `(filter?: NotesFilter) => Promise<NoteInfo[]>` | The notes, oldest first, only those on a source or file when the filter names one. Needs notes.read. |
| `create` | `(note: NewNote) => Promise<NoteInfo>` | Adds a note, on the active source unless it names another. Needs notes.write. |

### CommentsApi

Comments the person may push to the host.

| Member | Type | What |
| --- | --- | --- |
| `draft` | `(comment: NewNote) => Promise<NoteInfo>` | Starts a comment draft, which stays on this machine until the person stages and pushes it. Needs comments.draft. |

### NotesFilter

Which notes app.notes.list gives.

| Member | Type | What |
| --- | --- | --- |
| `source?` | `string` | Only those on the source with this ref. |
| `path?` | `string` | Only those on this file. |

### NewNote

A note or comment draft to add: on lines of a file, or on the source as a whole.

| Member | Type | What |
| --- | --- | --- |
| `text` | `string` | Its text, as Markdown. |
| `source?` | `string` | The ref of the source it is on; the active source when absent. |
| `path?` | `string` | The file it is on. |
| `line?` | `number` | The line it is on, from 1; needs path. |
| `startLine?` | `number` | The first line of a range ending at line. |
| `side?` | `NoteSide` | The side of the change its lines are on; new when absent. |

### FilesApi

Files a plugin reads and writes through Arc. Paths are absolute or relative to the plugin's folder.

| Member | Type | What |
| --- | --- | --- |
| `read` | `(path: string) => Promise<string>` | A file's text, under a folder the manifest lets it read. |
| `write` | `(path: string, text: string) => Promise<void>` | Writes a file's text, under a folder the manifest lets it write. |

### FetchInit

How app.fetch sends a request.

| Member | Type | What |
| --- | --- | --- |
| `method?` | `string` | GET when absent. |
| `headers?` | `Readonly<Record<string, string>>` | Request headers. |
| `body?` | `string` | The request's body as text. |

### FetchResponse

What app.fetch answers with.

| Member | Type | What |
| --- | --- | --- |
| `status` | `number` | The HTTP status. |
| `ok` | `boolean` | Whether the status is 200 to 299. |
| `headers` | `Readonly<Record<string, string>>` | The first value of each header, by lowercase name. |
| `text` | `() => Promise<string>` | The body as text. |
| `json` | `<T = unknown>() => Promise<T>` | The body read as JSON. |

### AppEvent

Something that happened in Arc.

| Member | Type | What |
| --- | --- | --- |
| `name` | `AppEventName` | What happened. |
| `source` | `SourceInfo \| null` | The source it happened in. |
| `file` | `FileInfo \| null` | The file it happened to. |
| `data` | `Readonly<Record<string, unknown>>` | What else it carries. |

## Events

| Name | What |
| --- | --- |
| `source.opened` | A source opened in the strip; event.source names it. |
| `file.marked` | A file was marked or unmarked; event.file names it, event.data.kind the mark or null. |
| `push.finished` | A push of a review to its host ended; event.source names the source. |
| `notes.changed` | The person's notes on a source changed; event.data.ids names them. Needs notes.read. |

## Data

- `SettingType`: `"text" | "number" | "boolean" | "choice" | "glob" | "list" | "map" | "secret"`
- `NoteKind`: `"note" | "draft"`
- `NoteSide`: `"old" | "new"`

### RepoInfo

| Field | Type |
| --- | --- |
| `id` | `string` |
| `name` | `string` |
| `githubId?` | `number` |

### SourceInfo

| Field | Type |
| --- | --- |
| `ref` | `string` |
| `kind` | `string` |
| `repo` | `RepoInfo` |
| `worktree?` | `string` |
| `head?` | `string` |

### FileInfo

| Field | Type |
| --- | --- |
| `path` | `string` |
| `line?` | `number` |

### GrepHit

| Field | Type |
| --- | --- |
| `path` | `string` |
| `line` | `number` |
| `text` | `string` |

### NoteInfo

| Field | Type |
| --- | --- |
| `id` | `string` |
| `kind` | `NoteKind` |
| `source` | `string` |
| `repo` | `RepoInfo` |
| `text` | `string` |
| `createdAt` | `number` |
| `updatedAt` | `number` |
| `path?` | `string` |
| `line?` | `number` |
| `startLine?` | `number` |
| `side?` | `NoteSide` |
| `revision?` | `string` |

## Icons

`branch`, `check`, `close`, `code`, `comment`, `commit`, `console`, `file`, `filter`, `folder`, `info`, `link`, `note`, `play`, `plugin`, `plus`, `pull-request`, `refresh`, `runs`, `search`, `settings`, `spec`, `stop`, `tag`, `warning`

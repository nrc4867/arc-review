import { useEffect, useState } from "react";
import type { App, AppEvent, AppEventName, FetchInit, FetchResponse, GrepHit, NewNote, NoteInfo, NotesFilter } from "../generated/arc";
import type { Channel } from "./channel";
import type { Listeners } from "./Listeners";
import type { Processes } from "./processes";
import { Methods, type CallContext } from "./protocol";

/** The statuses fetch counts as ok. */
const OK_FIRST = 200;
const OK_LAST = 299;

/** What every app object of one plugin shares. */
export interface Shared {
    readonly channel: Channel;
    readonly plugin: { readonly id: string; readonly version: string };
    readonly processes: Processes;
    readonly events: Listeners<AppEventName, [AppEvent]>;
    readonly settingsChanged: Listeners<"change", [Record<string, unknown>]>;
    settings(): Record<string, unknown>;
}

/** The app object for a call on [context]; a pane's [props] come with it. Every request carries the context. */
export function createApp(shared: Shared, context: CallContext, props: Record<string, unknown> = {}): App<any> {
    const { channel } = shared;
    return {
        plugin: shared.plugin,
        activeSource: context.source ?? null,
        focus: { file: context.file ?? null },
        props,
        get settings() {
            return shared.settings();
        },
        setSetting: (key: string, value: unknown) => channel.request(Methods.settingsSet, { key, value }),
        onSettingsChange: (listener: (settings: any) => void) => shared.settingsChanged.add("change", listener),
        store: {
            get: (key: string) => channel.request(Methods.storeGet, { key }).then((value: any) => value ?? undefined),
            set: (key: string, value: unknown) => channel.request(Methods.storeSet, { key, value }),
            delete: (key: string) => channel.request(Methods.storeDelete, { key }),
        },
        console: {
            run: async (command: readonly string[], options?: { in?: string }) => {
                const result = await channel.request(Methods.consoleRun, { command, cwd: options?.in ?? "worktree", context });
                return shared.processes.handle(result.handle);
            },
            attach: (id: string) => shared.processes.handle(id),
        },
        panes: {
            open: (pane: string, paneProps?: Record<string, unknown>) => channel.request(Methods.panesOpen, { pane, props: paneProps ?? {}, context }),
        },
        git: {
            grep: async (pattern: string, options?: { limit?: number }): Promise<GrepHit[]> => {
                const result = await channel.request(Methods.gitGrep, { pattern, context, ...(options?.limit ? { limit: options.limit } : {}) });
                return result.hits;
            },
        },
        notes: {
            list: async (filter?: NotesFilter): Promise<NoteInfo[]> => (await channel.request(Methods.notesList, { ...filter })).notes,
            create: (note: NewNote): Promise<NoteInfo> => channel.request(Methods.notesCreate, { ...note, context }),
        },
        comments: {
            draft: (comment: NewNote): Promise<NoteInfo> => channel.request(Methods.commentsDraft, { ...comment, context }),
        },
        files: {
            read: async (path: string) => (await channel.request(Methods.filesRead, { path })).text,
            write: async (path: string, text: string) => void (await channel.request(Methods.filesWrite, { path, text })),
        },
        fetch: async (url: string, init?: FetchInit): Promise<FetchResponse> => {
            const answer = await channel.request(Methods.netFetch, { url, method: init?.method ?? "GET", headers: init?.headers ?? {}, body: init?.body ?? null });
            const headers = Object.fromEntries(Object.entries(answer.headers ?? {}).map(([name, value]) => [name.toLowerCase(), value as string]));
            const body: string = answer.body ?? "";
            return { status: answer.status, ok: answer.status >= OK_FIRST && answer.status <= OK_LAST, headers, text: async () => body, json: async () => JSON.parse(body) };
        },
        on: (event: AppEventName, listener: (event: AppEvent) => void) => shared.events.add(event, listener),
        useQuery: <T>(load: () => Promise<T>, deps: readonly unknown[] = []) => {
            const [value, setValue] = useState<T | undefined>(undefined);
            useEffect(() => {
                let live = true;
                load().then(
                    (loaded) => live && setValue(loaded),
                    (error) => console.error(error),
                );
                return () => {
                    live = false;
                };
            }, [context.source?.ref, ...deps]);
            return value;
        },
        log: (...values: unknown[]) => console.error(...values),
    } as App<any>;
}
